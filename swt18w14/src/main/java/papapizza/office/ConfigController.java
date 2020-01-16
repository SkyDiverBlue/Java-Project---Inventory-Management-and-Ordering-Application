package papapizza.office;


import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class reads and writes on files
 */
class ConfigController {


	private File file;
	private FileWriter writer;
	private String fileName;
	private String configInformation= "";

	protected ConfigController (String fileName){
		this.fileName = fileName;
	}

	ConfigController (){	}

	/**
	 * depending on the system the absolute filepath changes
	 * @return filepath
	 */
	private String getFilePath(){
		String os = System.getProperty("os.name");

		if (os.contains("Windows")){
			return System.getProperty("user.dir") + "\\src\\main\\java\\papapizza\\office\\config.txt";
		}
		else {
			return  System.getProperty("user.dir") +"/src/main/java/papapizza/office/config.txt";
		}
	}


	/**
	 * the method overwrites the Config file
	 * @param config
	 * @param value
	 */
	public void overWriteConfigFile(String config, String value)  {


		ArrayList<String> allData = new ArrayList<>();
		for (String specificData: getFileData()){
			if (!specificData.contains(config)){
				allData.add(specificData);
				System.out.println(specificData);
			}
			else {
				allData.remove(specificData);
				allData.add(config + value);
			}
		}


		if (config == null) throw new NullPointerException();
		file = new File(getFilePath());
		try {
			writer = new FileWriter(file ,false);
			for (String data : allData) {
				writer.write(data);
				writer.write(System.getProperty("line.separator"));
				writer.flush();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	/**
	 * this method ready a config file and returns the required number
	 * @param nameConvention
	 * @return
	 */
	public String readFile(String nameConvention) {


		ArrayList<String> allLines = getFileData();


		String information = "";

		for (String line: allLines) {
			if (line.contains(nameConvention)) {
				String[] splitter = line.split(" = ");
				for (String string : splitter) {
					//string = string.replace("\n", "");
					if (!string.contains(nameConvention)) {
						information = string;
					}
				}
			}

		}



		return information;
	}


	/**
	 * converts a file into an array of Strings
	 * @return
	 */
	private ArrayList<String> getFileData(){
		FileReader fr = null;
		BufferedReader br = null;
		ArrayList<String> allLines = new ArrayList<>();
		try
		{
			File file = new File(getFilePath()) ;
			fr = new FileReader(file);
			br = new BufferedReader(fr) ;

			String line ;
			StringBuffer sb = new StringBuffer();
			//String sep = System.getProperty("line.separator");

			while ((line = br.readLine()) != null)   {
				allLines.add(line);
			}
		}
		catch(IOException ex) {
			System.out.println(ex);
		}
		finally {
			try {
				if(br!=null) br.close();
				if(fr!=null) fr.close();
			}
			catch(Exception ex) {
				System.out.println("closing went wrong" + ex);
			}
		}
		return allLines;
	}

}
