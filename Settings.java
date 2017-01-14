import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
//Класс настроек (сохраняет и загружает настройки)
public class Settings {
	String inFolder;
	String outFolder;
	int max_process;
	boolean update,old_update,delete=true;
	
	Settings() throws IOException{
		inFolder=new String();
		outFolder=new String();
		File file=new File("settings.ini");
		if(file.exists()){
			BufferedReader bfw=new BufferedReader(new FileReader(file));
			max_process=Integer.parseInt(bfw.readLine());
			if(bfw.readLine().equals("true"))update=true;
			else update=false;
			if(bfw.readLine().equals("true"))old_update=true;
			else old_update=false;
			if(bfw.readLine().equals("true"))delete=true;
			else delete=false;
			inFolder=bfw.readLine();
			outFolder=bfw.readLine();
		}else{
			update=false;
			old_update=false;
			delete=false;
			inFolder="Ïàïêà íå âûáðàíà";
			outFolder="Ïàïêà íå âûáðàíà";
			max_process=4;
		}
	}
	
	public void save() throws IOException{
		File file=new File("settings.ini");
		if(!file.exists())file.createNewFile();
		BufferedWriter bfw=new BufferedWriter(new FileWriter(file));
		bfw.write(""+max_process);
		bfw.newLine();
		bfw.write(""+update);
		bfw.newLine();
		bfw.write(""+old_update);
		bfw.newLine();
		bfw.write(""+delete);
		bfw.newLine();
		bfw.write(inFolder);
		bfw.newLine();
		bfw.write(outFolder);
		bfw.close();
	}
}
