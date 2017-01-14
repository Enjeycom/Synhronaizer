import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.locks.ReentrantLock;
//Класс - копирования (каждый объект - поток)
public class Copyer extends Thread{
	File in;
	File out;
	Integer process_started;
	ReentrantLock lock;
	Copyer(File in,File out,Integer process_started,ReentrantLock lock){
		this.in=in;
		this.out=out;
		this.process_started=process_started;
		this.lock=lock;
	}
	@Override
	public void run(){
		lock.lock();
		process_started++;
		lock.unlock();
		//копирование
	    try {
			FileInputStream fis = new FileInputStream(in);
			FileOutputStream fos = new FileOutputStream(out);
			byte buffer[] = new byte[1024];
			int size=0;
			do{
				size=fis.read(buffer,0,1024);
				if(size>0)
					fos.write(buffer, 0, size);
			}while(size>0);
		} catch (IOException e1) {}
		
	    
		lock.lock();
		process_started--;
		lock.unlock();
	}
}
