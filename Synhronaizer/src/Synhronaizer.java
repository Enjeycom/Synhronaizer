import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

class Copy extends Thread{
	File in;
	File out;
	Integer process_started;
	ReentrantLock lock;
	Copy(String pathIn,String pathOut,Integer process_started,ReentrantLock lock){
		in=new File(pathIn);
		out=new File(pathOut);
		this.process_started=process_started;
		this.lock=lock;
	}
	@Override
	public void run(){
		lock.lock();
		process_started++;
		lock.unlock();
		try {
			Files.copy(in.toPath(),out.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
		lock.lock();
		process_started--;
		lock.unlock();
	}
}

public class Synhronaizer extends Thread{
	float time=System.nanoTime();
	Settings settings;
	GUI gui;
	Integer n_synh_files,n_delete_files,process_started;
	ReentrantLock lock;
	ArrayList<Thread> tasks;
	Synhronaizer(Settings settings,GUI gui){
		tasks=new ArrayList<Thread>();
		lock=new ReentrantLock();
		this.settings=settings;
		this.gui=gui;
		n_synh_files=new Integer(0);
		process_started=new Integer(0);
		n_delete_files=new Integer(0);
	}
	
	private String oldAdress(String adress){
		return (settings.inFolder+adress.substring(settings.outFolder.length(),adress.length()));
	}
	
	private String newAdress(String adress){
		return (settings.outFolder+adress.substring(settings.inFolder.length(),adress.length()));
	}
	
	void copy(String pathIn,String pathOut) throws IOException{
		gui.infoPanel.setInfo("Копирование "+pathIn);
		File in=new File(pathIn);
		File out=new File(pathOut);
		Files.copy(in.toPath(),out.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}
	
	void create(String pathOut){
		new File(pathOut).mkdir();
	}
	
	public  void scanFolderDelete(File folder) throws IOException{
		File files[]=folder.listFiles();
		for(int i=0;i<files.length;i++){
			File tmp=new File(oldAdress(files[i].getAbsolutePath()));
			if(files[i].isDirectory()){
				scanFolderDelete(files[i]);
			}else
				if(!tmp.exists()){
					Files.delete(files[i].toPath());
				}
		}
	}
	
	public  void scanFolder(File folder) throws IOException{
		gui.infoPanel.setInfo("Синхронизация папки: "+ folder.getAbsolutePath());
		File files[]=folder.listFiles();
		for(int i=0;i<files.length;i++){
			File tmp=new File(newAdress(files[i].getAbsolutePath()));
			if(files[i].isDirectory()){
				if(!tmp.exists()){
					create(tmp.getAbsolutePath());
				}
				scanFolder(files[i]);
			}else
				if(!tmp.exists()||(settings.update&&!settings.old_update)||(settings.old_update&&tmp.lastModified()<files[i].lastModified())){
					while(process_started>settings.max_process){}
					tasks.add(new Copy(files[i].getAbsolutePath(),tmp.getAbsolutePath(),process_started,lock));
					tasks.get(tasks.size()-1).start();
					n_synh_files++;
				}
		}
		
	}
	@Override
	public void run(){
			gui.setContentPane(gui.infoPanel);
			gui.infoPanel.setInfo("Синхронизация...");
			gui.setVisible(true);
			try {
				scanFolder(new File(settings.inFolder));
				for(Thread t:tasks)t.join();
				gui.infoPanel.setInfo("Удаление...");
				if(settings.delete)
					scanFolderDelete(new File(settings.outFolder));
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
			gui.infoPanel.setInfo("Синхронизация завершена!\nСинхронизированно " + n_synh_files+" файлов\nУдалено " + n_delete_files+" файлов\nПрошло вермени: "+(System.nanoTime()-time));
	}
}
