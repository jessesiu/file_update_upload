package file_update;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;


import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.xml.sax.SAXException;

import org.apache.commons.net.ftp.*;

public class upload_file {

	

	static String[] comExt = { "7z", "arj", "bz2", "bzip2", "cab", "cpio", "deb", "dmg", "gz", "gzip", "hfs", "iso",
			"lha", "lzh", "lzma", "rar", "rpm", "split", "swm", "tar", "taz", "tbz", "tbz2", "tgz", "tpz", "wim", "xar",
			"z", "zip", "mzML" };
	static HashSet<String> compressExtension = new HashSet<String>(Arrays.asList(comExt));

	public static void main(String[] args)
			throws SQLException, SAXException, IOException, ParserConfigurationException, java.text.ParseException

	{

		Setting.Loadsetting();
		String ftp = Setting.prefix + Setting.doi;
		String filename = null;
		String fileextension = null;
		String fileformat = null;
		String location = null;
		int filetype = 0;
		int fileformatid = 0;
		long size = -1;
		Extension2Type extension2Type = new Extension2Type();

		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String date_time = formatter.format(calendar.getTime());
		System.out.println(date_time);

		String description = null;
		String code = null;
		database_v1 db = new database_v1();
		db.delete_file();
		int datasetid = db.getdataset_id(Setting.doi);
		int file_id = db.getid("file");
		File file = new File(Setting.path);
		if (!file.isFile()) {
			throw new RuntimeException(file + "xxx");
		}
		POIFSFileSystem fs = null;
		HSSFWorkbook wb = null;
		HSSFSheet sheet = null;
		try {
			fs = new POIFSFileSystem(new FileInputStream(file));
			wb = new HSSFWorkbook(fs);
			sheet = wb.getSheetAt(0);
		} catch (IOException e) {
			System.out.println(file);
			e.printStackTrace();
		}

		ArrayList<Integer> attributes_id = new ArrayList<Integer>();

		for (Row row : sheet) {

			if (row.getRowNum() == 0) {
				for (Cell cell : row) {

					int columnIndex = cell.getColumnIndex();
					if (columnIndex > 3) {
						String value = cell.getStringCellValue();
						int att_id = db.getattribute_id(value);
						if (att_id == 0) {

							att_id = db.add_attribute(value);
							System.out.println("！！！！！！！！！！！！！！！new add: " + value + " " + att_id);

						}
						attributes_id.add(att_id);
					}
				}
			} else {

				for (Cell cell : row) {
					int columnIndex = cell.getColumnIndex();

					if (columnIndex == 0) {

						location = cell.getStringCellValue();
						if (location.startsWith("/") == false) {
							location = "/" + location;
						}

						filename = getFile_name(location);																												
						location = ftp + location;
						fileextension = getFile_extension(filename, extension2Type);

						//size = 0;
						size= getFile_size(location); //in reall time
						String temp = Long.toString(size);
						System.out.println("test temp: " + temp);

						// newsize = Integer.parseInt(temp);

						fileformat = getFile_format(fileextension, extension2Type);
						fileformatid = db.getformatid(fileformat);
						System.out.println("DOI: " + datasetid);
						System.out.println("location: " + location);
						System.out.println("filename: " + filename);
						System.out.println("extension: " + fileextension);
						System.out.println("format: " + fileformatid);
						System.out.println("size: " + size);

					}
					if (columnIndex == 1) {
						filetype = db.gettypeid(cell.getStringCellValue());
						System.out.println("type: " + filetype);
					}

					if (columnIndex == 2) {
						description = cell.getStringCellValue();
						System.out.println("description: " + description);
					}
					if (columnIndex == 3) {
						if (cell.getCellType() == 0) {
							code = String.valueOf((int) cell.getNumericCellValue());
							System.out.println("code: " + code);
						} else {
							code = cell.getStringCellValue();
							System.out.println("code: " + code);
						}

						db.addfilev3(file_id, datasetid, filename, location, fileextension, size, description,
								date_time, fileformatid, filetype, code);
						description = "";

					}
					/*
					 * if(columnIndex == 4) { location=
					 * cell.getStringCellValue();
					 * System.out.println("location: "+ location); }
					 */

					if (columnIndex > 3) {
						String value = "";
						if (cell.getCellType() == 0) {
							value = String.valueOf(cell.getNumericCellValue());
							if (value.endsWith(".0")) {
								value = value.replace(".0", "");
							}

							System.out.println(value);

						} else {
							value = cell.getStringCellValue();
							System.out.println(value);
						}
						if (value == "" || value.isEmpty() || value == null) {
							continue;
						} else {

							db.addfile_attribute(file_id, attributes_id.get(columnIndex - 4), value);
						}
					}

				}

				file_id++;

			}

		}
		db.updateids();
		db.close();
	}

	public static String getFile_name(String file_path) {
		int beginIndex = file_path.lastIndexOf("/") + 1;
		return file_path.substring(beginIndex);
	}

	public static String getFile_extension(String file_name, Extension2Type extension2Type) throws IOException {
		// file_name="asdfasd.txt";
		String[] extensionArray = file_name.split("\\.");
		String extension = "";
		int length = extensionArray.length;
		if (length == 1) {
			System.out.println("Getting file_extension, file_name: " + file_name + " Warning there is not '.' in it!");
			return "unknown";
		}
		// the first one shouldn't be extension
		for (int i = 1; i < length; i++) {
			String temp = trim(extensionArray[i]);
			// all extension are lower case in map, so when camparing,
			// I need to change temp to lowercase
			// if readme then the extension before it is removed
			if (temp.equals("readme")) {
				extension = "";
				continue;
			}
			if (extension2Type.map.keySet().contains(temp.toLowerCase())) {
				if (extension != "" && temp.equals("txt"))
					continue;
				extension = temp;
			}
		}
		if (extension == "") {
			int index = length - 1;
			while (compressExtension.contains(extensionArray[index].toLowerCase()))
				index--;
			extension = extensionArray[index];
		}
		return extension;
	}

	public static String getFile_format(String file_extension, Extension2Type extension2Type) {
		// to lower case
		if (extension2Type.map.containsKey(file_extension.toLowerCase()))
			return extension2Type.map.get(file_extension.toLowerCase());
		else
			return "UNKNOWN";
	}

	public static String trim(String string) {
		int beginIndex = 0;
		for (int i = 0; i < string.length(); i++) {
			char temp = string.charAt(i);
			if (Character.isWhitespace(temp) || temp == '\u00a0' || temp == '\u2007' || temp == '\u202f') {
				beginIndex++;
			} else
				break;
		}
		int endIndex = string.length();
		for (int i = string.length(); i > 0; i--) {
			char temp = string.charAt(i - 1);
			if (Character.isWhitespace(temp) || temp == '\u00a0' || temp == '\u2007' || temp == '\u202f') {
				endIndex--;
			} else
				break;
		}
		if (endIndex <= beginIndex)
			return "";
		return string.substring(beginIndex, endIndex);
	}

	public static long getFile_size(String file_location) throws IOException {
		// String
		// ftp_site=(String)schema.getTable("dataset").getAttribute("ftp_site");
		file_location = file_location.replace("ftp://penguin.genomics.cn", "ftp://parrot.genomics.cn/gigadb");

		String ftp_site = "parrot.genomics.cn";
		System.out.println(file_location);

		// String path = ftp_site + file_location;
		// FtpClient ftpClient = new FtpClient();
		file_location = file_location.trim();

		FTPClient ftpClient = new FTPClient();

		// ftpClient.openServer(ftp_site);
		ftpClient.connect(ftp_site);
		// ftpClient.connect(ftp_site1);//when use vpn
		long fileSize = -1;
		// ftpClient.login("senhong", "senhong1631");
		ftpClient.login("anonymous", "anonymous");
		ftpClient.enterLocalPassiveMode();
		// ftpClient.binary();;
		int beginIndex = 0;
		if (file_location.indexOf(ftp_site) != -1)
			beginIndex = file_location.indexOf(ftp_site) + ftp_site.length();
		String location = file_location.substring(beginIndex);
		String request = "SIZE " + location + "\r\n";
		// ftpClient.sendServer(request);
		ftpClient.sendCommand(request);
		// String temp = ftpClient.getResponseString();
		String temp = ftpClient.getReplyString();
		// int status = ftpClient.readServerResponse();

		int status = ftpClient.getReplyCode();
		if (status == 213) {
			String msg = ftpClient.getReplyString();
			// System.out.println(msg);
			fileSize = Long.parseLong(msg.substring(3).trim());
			// System.out.println(fileSize);
		} else {

			System.out.println("We can't get the file, please check its path: ");
			System.out.println(location);
		}
		ftpClient.disconnect();
		return fileSize;
	}

}
