import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JOptionPane;

//Класс сихронизации (создает потоки копирования)
public class Synhronaizer extends Thread{
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
		n_delete_files=new Integer(0);
		process_started=new Integer(0);
	}
	//функция возврашает старый адрес файла
	private String oldAdress(String adress){
		return (settings.inFolder+adress.substring(settings.outFolder.length(),adress.length()));
	}
	//функция возврашает новый адрес файла
	private String newAdress(String adress){
		return (settings.outFolder+adress.substring(settings.inFolder.length(),adress.length()));
	}
	//функция удаляет файлы отсутствующия в источнике
	public  void scanFolderDelete(File folder) throws IOException{
		gui.infoPanel.plusInfo("Óäàëåíèå â ïàïêå:",folder.getAbsolutePath());
		File files[]=folder.listFiles();
		for(int i=0;i<files.length;i++){
			File tmp=new File(oldAdress(files[i].getAbsolutePath()));
			if(files[i].isDirectory()){
				scanFolderDelete(files[i]);
			}else
				if(!tmp.exists()&&!tmp.getName().equals("Indexed Locations.search-ms")){
					gui.infoPanel.plusInfo("Óäàëåíèå ôàéëà",tmp.getAbsolutePath());
					Files.delete(files[i].toPath());
					n_delete_files++;
				}
		}
	}
	// функция создает новые потоки копирования и следит за их количеством
	void copy(File f1,File f2){
		while(process_started>settings.max_process){}
		tasks.add(new Copyer(f1,f2,process_started,lock));
		tasks.get(tasks.size()-1).start();
		n_synh_files++;
	}
	//функция реверсивно синхронизирует папки
	public  void scanFolder(File folder) throws IOException, NoSuchAlgorithmException{
		gui.infoPanel.plusInfo("Cèíõðîíèçàöèÿ ïàïêè:",folder.getAbsolutePath());
		File files[]=folder.listFiles();
		for(int i=0;i<files.length;i++){
			File tmp=new File(newAdress(files[i].getAbsolutePath()));
			if(files[i].isDirectory()){
				if(!tmp.exists()){
					new File(tmp.getAbsolutePath()).mkdir();
				}
				scanFolder(files[i]);
			}else{
				// случаи в которых файл копируется:
				if(!tmp.exists() || (settings.update&&!settings.old_update)){
					copy(files[i],tmp); //Копирование в случае если файла нет либо стоит галка заменять файлы
				}else
				if((settings.old_update&&tmp.lastModified()<files[i].lastModified())){
					copy(files[i],tmp); //Копирование в случае если файл существует и стоит галка заменять устаревгие файлы
				}else
				if(settings.old_update&&tmp.lastModified()==files[i].lastModified()){
					if(!controlSumChek(tmp,files[i]))
						copy(files[i],tmp); //Копирование в случае если файл существует, стоит галка заменять устаревгие файлы и файлы имеют одинаковую дату изменения
				}	
			}
		}
		
	}
	//функция считает контрольную сумму
	public static String getControlSum(File file) throws NoSuchAlgorithmException, IOException {
         final MessageDigest md = MessageDigest.getInstance("SHA-1");
         final FileInputStream fis = new FileInputStream(file);
         byte[] dataBytes = new byte[1024];
         int bytesRead;
         while((bytesRead = fis.read(dataBytes)) > 0)
             md.update(dataBytes, 0, bytesRead);
         byte[] mdBytes = md.digest();
         StringBuilder sb = new StringBuilder();
         for(int i = 0; i < mdBytes.length; i++)
             sb.append(Integer.toString((mdBytes[i] & 0xff) + 0x100, 16).substring(1));
         return sb.toString();
	}
	//фунция сравнивает контрольные суммы
	private boolean controlSumChek(File f1, File f2) throws NoSuchAlgorithmException, IOException {
		if(getControlSum(f1).equals(getControlSum(f2))) 
			return true;
		return false;
	}
	//поток синхронизации
	@Override
	public void run(){
			if(!new File(settings.inFolder).exists()||!new File(settings.outFolder).exists()){
				JOptionPane.showMessageDialog(gui,"Вы не выбрали папки или папки не существуют!","Ошибка!",JOptionPane.WARNING_MESSAGE);
				return;
			}
			if(settings.inFolder.equals(settings.outFolder)){
				JOptionPane.showMessageDialog(gui,"Папке не нужно синхронизироватся самой с собой!","Ошибка!",JOptionPane.WARNING_MESSAGE);
				return;
			}
			double time=System.currentTimeMillis();
			gui.setContentPane(gui.infoPanel);
			gui.infoPanel.ok.setEnabled(false);
			gui.infoPanel.plusInfo("Синхронизация...","");
			gui.setVisible(true);
			try {
				scanFolder(new File(settings.inFolder));
				for(Thread t:tasks)
					t.join();
			} catch (InterruptedException | NoSuchAlgorithmException | IOException e) {
				e.printStackTrace();
			}
			
			gui.infoPanel.plusInfo("Удаление...","");
			if(settings.delete)
				try {
					scanFolderDelete(new File(settings.outFolder));
				} catch (IOException e) {
					e.printStackTrace();
				}
			gui.infoPanel.ok.setEnabled(true);
			gui.infoPanel.setInfo("Синхронизация завершена!\nСинхронизированно " + n_synh_files+" файлов\nУдалено " + n_delete_files+" ôàéëîâ\nÏðîøëî âåðìåíè: "+(System.currentTimeMillis()-time)/1000+" ñåêóíä");
	}
}
