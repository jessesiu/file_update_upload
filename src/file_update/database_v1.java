package file_update;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.util.IOUtils;
import org.xml.sax.SAXException;


public class database_v1 {

	Connection con;
	String url ;
	String password ;
	String user ;
	String doi;
	static Statement stmt;
	Statement stmt1;
	PreparedStatement prepforall = null;

	public database_v1() throws ParserConfigurationException, SAXException, IOException {
		
		Setting.Loadsetting();
		try {
			Class.forName("org.postgresql.Driver").newInstance();
			url = Setting.databaseUrl;
			user = Setting.databaseUserName;
			password = Setting.databasePassword;
			doi = Setting.doi;
			con = DriverManager.getConnection(url, user, password);
			con.setAutoCommit(true);
			stmt = con.createStatement();
			stmt1 = con.createStatement();



		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void delete_file() throws FileNotFoundException, IOException, SQLException
	{
		
		PreparedStatement prep1= null;
		String query= "delete from file_attributes where file_id in (select id from file where dataset_id in (select id from dataset where identifier="+"'"+doi+"'"+"))";
		System.out.println(query);
		prep1= con.prepareStatement(query);
		prep1.executeUpdate();
		prep1.close();
		query= "delete from file_sample where file_id in (select id from file where dataset_id in (select id from dataset where identifier="+"'"+doi+"'"+"))";
		System.out.println(query);
		prep1= con.prepareStatement(query);
		prep1.executeUpdate();
		prep1.close();
		query= "delete from file where dataset_id = (select id from dataset where identifier ="+"'"+doi+"'"+")";
		System.out.println(query);
		prep1= con.prepareStatement(query);
		prep1.executeUpdate();
		prep1.close();
		
	}
	
	public void updateids() throws FileNotFoundException, IOException, SQLException
	{

		ScriptRunner sr = new ScriptRunner(con,false,false);
		sr.runScript(new BufferedReader(new FileReader("./files/updateid.sql")));
	}
	
	

	public int getid(String table) throws SQLException {
		int newid = 0;
		String query = "SELECT max(id) from " + table;
		System.out.println("query " + query);
		ResultSet resultSet = stmt.executeQuery(query);
		while (resultSet.next()) {
			query = resultSet.getString("max");
		}
		if (query == null)
			newid = 1;
		else
			newid = Integer.valueOf(query) + 1;

		System.out.println("id number in db " + newid);
		return newid;

	}


	public int getattribute_id(String name) throws SQLException {
		String query = "select id from attribute where lower(attribute_name)=" + "lower('" + name + "');";
		// System.out.println("query"+query);
		ResultSet resultSet = stmt.executeQuery(query);

		int id = 0;
		while (resultSet.next()) {
			id = resultSet.getInt("id");

		}
		if (id == 0) {

			String query1 = "select id from attribute where lower(structured_comment_name)=" + "lower('" + name + "');";
			// System.out.println("query"+query);
			ResultSet resultSet1 = stmt.executeQuery(query1);

			while (resultSet1.next()) {
				id = resultSet1.getInt("id");

			}
		}

		return id;

	}
	
	public int getdataset_id(String doi) throws SQLException
	{
		String query="select id from dataset where identifier="+"'"+doi+"'"+";";
		//System.out.println("query"+query);
		ResultSet resultSet=stmt.executeQuery(query);
		
		int id=0;
		while(resultSet.next())
		{
			id= resultSet.getInt("id");
	
		}
		
		return id;

	}


	public int add_attribute(String name) throws SQLException {
		int id = this.getid("attribute");
		String query1 = "insert into attribute(id,attribute_name) values(?,?)";
		PreparedStatement prep1 = null;
		prep1 = con.prepareStatement(query1);
		prep1.setInt(1, id);
		prep1.setString(2, name);
		prep1.executeUpdate();

		return id;
	}

	
	
	public void addfile_attribute(int file_id,int attribute_id, String value) throws SQLException
	{
	  
		
		PreparedStatement prep1= null;
        String query1="insert into file_attributes(file_id, attribute_id, value) values(?,?,?)";
        
		System.out.println(query1);
		prep1= con.prepareStatement(query1);
		prep1.setInt(1, file_id);
		prep1.setInt(2, attribute_id);
		prep1.setString(3, value);
		
		prep1.executeUpdate();
		
	
	}
	
	public int gettypeid(String name) throws SQLException
	{
		String query="select * from file_type where name="+"'"+name+"';";
		//System.out.println("query"+query);
		ResultSet resultSet=stmt.executeQuery(query);
		
		int id=0;
		while(resultSet.next())
		{
			id= resultSet.getInt("id");
	
		}
		if(id==0)
		{
		    
			addtype(name);
			resultSet=stmt.executeQuery(query);
			
			
			while(resultSet.next())
			{
				id= resultSet.getInt("id");
		
			}
		}
		return id;
		
	}
	
	public void addtype(String name) throws SQLException
	{
		
		PreparedStatement prep1= null;
        String query1="insert into file_type(name) values(?)";
        
		System.out.println(query1);
		prep1= con.prepareStatement(query1);
		prep1.setString(1, name);
		
		prep1.executeUpdate();
			
	}
	
	public void addfilev3(int file_id,int dataset_id, String name, String location, String extension, long size, String description, String date_stamp,int format_id, int type_id,String code) throws SQLException
	{
		int id= getid("file");
	    
	   
		PreparedStatement prep1= null;
        String query1="insert into file(id,dataset_id, name, location, extension, size, description,date_stamp, format_id, type_id) values(?,?,?,?,?,?,?,?,?,?)";
        java.sql.Date date= java.sql.Date.valueOf(date_stamp);
		System.out.println(query1);
		prep1= con.prepareStatement(query1);
		prep1.setInt(1, file_id);
		prep1.setInt(2, dataset_id);
		prep1.setString(3, name);
		prep1.setString(4, location);
		prep1.setString(5, extension);
		prep1.setLong(6,size);
		prep1.setString(7,description);
		prep1.setDate(8,date);
		prep1.setInt(9, format_id);
		prep1.setInt(10, type_id);
		
		prep1.executeUpdate();
		if(code!=null){
		code= code.trim();
		}
		
	   if(code!=null && code != "" && !code.isEmpty())
	   {
		   
	   
		   String aa[]= code.split(";");
		   for(String sample_name: aa)
		   {  
			  int sample_id=0;
			  sample_name= sample_name.trim();
			  System.out.println("sample_name: "+sample_name);
			  if(sample_name ==null || sample_name == "")
				  continue;
		      sample_id=file_get_sampleid(dataset_id, sample_name);
		      if(file_id!=0 && sample_id !=0)
		      {
		    	  addfile_sample(sample_id,file_id);
		      }
		      else
		      {
		    	  System.out.println("Can't find sample id or file id");
		      }
		   }
		   
		   
	   }
		
	
	}
	
	public int file_get_sampleid(int datasetid, String code) throws SQLException
	{
		String query="select sample.id from sample, dataset_sample where sample.id=dataset_sample.sample_id and sample.name like "+"'"+code+"%'"+" and dataset_sample.dataset_id="+datasetid+";";
		System.out.println("query"+query);
		ResultSet resultSet=stmt.executeQuery(query);
		
		int id=0;
	
		while(resultSet.next())
		{
			id=resultSet.getInt("id");
		
		}
		if(id==0)
		{
			System.out.println("Can't find sample in talble" + code);
		}
		
		System.out.println(id);
		return id;

	}
	
	public void addfile_sample(int sample_id, int file_id) throws SQLException
	{
	    
		
		PreparedStatement prep1= null;
        String query1="insert into file_sample(sample_id, file_id) values(?,?)";
        
		System.out.println(query1);
		prep1= con.prepareStatement(query1);
		prep1.setInt(1, sample_id);
		prep1.setInt(2, file_id);
		
		
		prep1.executeUpdate();
		
		
	
	}
	public int getformatid(String name) throws SQLException
	{
		String query="select * from file_format where name="+"'"+name+"';";
		//System.out.println("query"+query);
		ResultSet resultSet=stmt.executeQuery(query);
		
		int id=0;
		while(resultSet.next())
		{
			id= resultSet.getInt("id");
	
		}
		if(id==0)
			System.out.println("Need add new format "+ name );
		return id;
		
	}

	public void close() throws SQLException {
		con.close();

	}

	public static void main(String[] args) throws Exception {
		database_v1 db = new database_v1();
	}

}
