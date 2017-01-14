import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
// панель вывода информации о копировании
class PanelInfo extends JPanel{
	private static final long serialVersionUID = 1L;
	GUI gui;
	Font font;
	JTextArea info;
	JButton ok;
	Settings settings;
	JScrollPane scrollPane;
	PanelInfo(GUI gui){
		this.gui=gui;
		font=new Font("Times new Roman",16,16);
		info=new JTextArea();
		ok=new JButton();
		
		info.setWrapStyleWord(true);
		info.setFont(font);
		info.setBounds(0,0,530,190);
		scrollPane = new JScrollPane(info);
		scrollPane.setBounds(0,0,530,190);
		ok.setBounds(420, 190, 100, 30);
		ok.setText("Ok");
		ok.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				gui.setContentPane(gui.controlPanel);
			}
		});
		setLayout(null);
		add(info);
		add(ok);
	}
	public void setInfo(String str){
		info.setText(str);
	}
	
	String getEndString(String a){
		if(a.length()<46)
			return " "+a.substring(0,a.length());
		return " ..."+a.substring(a.length()-46,a.length());
	}

	
	public void plusInfo(String str1, String str2) {
		info.setText(str1+getEndString(str2)+"\n"+info.getText());
	}
}
// панель выбора настроек
class PanelControl extends JPanel{
	private static final long serialVersionUID = 1L;
	JButton chose_folder1,chose_folder2,sinhronaiz;
	JLabel folder1,folder2,potoki;
	JCheckBox unf,uonf,df;
	Font font;
	JFileChooser fc;
	GUI gui;
	JSlider slider;
	PanelControl(GUI gui){
		this.gui=gui;
		fc=new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		font=new Font("Times new Roman",16,16);
		chose_folder1=new JButton();
		chose_folder2=new JButton();
		sinhronaiz=new JButton();
		folder1=new JLabel();
		folder2=new JLabel();
		potoki=new JLabel();
		unf=new JCheckBox();
		uonf=new JCheckBox();
		df=new JCheckBox();
		slider=new JSlider();
		
		folder1.setFont(font);
		folder2.setFont(font);
		unf.setFont(font);
		uonf.setFont(font);
		df.setFont(font);
		potoki.setFont(font);
		
		chose_folder1.setBounds(350, 10, 160, 30);
		chose_folder2.setBounds(350, 50, 160, 30);
		sinhronaiz.setBounds(350, 170, 160, 30);
		folder1.setBounds(10, 15, 310, 20);
		folder2.setBounds(10, 55, 310, 20);
		df.setBounds(10, 90, 340, 30);
		unf.setBounds(10, 130, 340, 30);
		uonf.setBounds(10, 170, 340, 30);
		potoki.setBounds(350,90,190,30);
		slider.setBounds(350,130,160,30);
		
		chose_folder1.setText("....");
		chose_folder2.setText("....");
		sinhronaiz.setText("Синхронизировать");
		folder1.setText(gui.settings.inFolder);
		folder2.setText(gui.settings.outFolder);
		unf.setText("Заменять существующие файлы");
		unf.setSelected(gui.settings.update);
		uonf.setText("Заменять толко устаревшие файлы");
		if(!gui.settings.update){
			uonf.setEnabled(false);
		}
		uonf.setSelected(gui.settings.old_update);
		df.setText("Удалять файлы отсутствующие в источнике");
		df.setSelected(gui.settings.delete);
		slider.setValue(gui.settings.max_process);
		potoki.setText("Количество потоков: "+slider.getValue());
		slider.setMaximum(16);
		slider.setMinimum(1);
		slider.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent arg0) {
				gui.settings.max_process=slider.getValue();
				potoki.setText("Количество потоков: "+slider.getValue());
			}
		});
		chose_folder1.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
					int ret=fc.showOpenDialog(fc);
					if(ret==JFileChooser.APPROVE_OPTION){
						gui.settings.inFolder=fc.getSelectedFile().getAbsolutePath();
						folder1.setText(gui.settings.inFolder);
					}
			}
		});
		chose_folder2.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
					int ret=fc.showOpenDialog(fc);
					if(ret==JFileChooser.APPROVE_OPTION){
						gui.settings.outFolder=fc.getSelectedFile().getAbsolutePath();
						folder2.setText(gui.settings.outFolder);
					}
			}
		});
		sinhronaiz.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {	
					new Synhronaizer(gui.settings,gui).start();
			}
		});
		unf.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				gui.settings.update=unf.isSelected();
				if(unf.isSelected()){
					gui.controlPanel.uonf.setEnabled(true);
				}else{
					gui.controlPanel.uonf.setEnabled(false);
					gui.controlPanel.uonf.setSelected(false);
					gui.settings.old_update=false;
				}
			}
		});
		
		uonf.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				gui.settings.old_update=uonf.isSelected();
			}
		});
		
		df.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				gui.settings.delete=df.isSelected();
			}
		});

		setLayout(null);
		add(chose_folder1);
		add(chose_folder2);
		add(sinhronaiz);
		add(folder1);
		add(folder2);
		add(unf);
		add(slider);
		add(uonf);
		add(df);
		add(potoki);
	}
}
//окно приложения
public class GUI extends JFrame{
	private static final long serialVersionUID = 1L;
	Settings settings;
	PanelControl controlPanel;
	PanelInfo infoPanel;
	GUI() throws IOException{
		settings=new Settings();
		controlPanel=new PanelControl(this);
		infoPanel=new PanelInfo(this);
		setContentPane(controlPanel);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setBounds(200,200,530,250);
		setTitle("Синхронизатор");
		addWindowListener(new WindowAdapter() {
		     public void windowClosing(WindowEvent event) {
		    	 try {
					settings.save();
				} catch (IOException e) {
					e.printStackTrace();
				}
		         System.exit(0);
		     }
		});
		setResizable(false);
		setVisible(true);
	}
	public static void main(String []args) throws IOException{
		new GUI();
	}
}
