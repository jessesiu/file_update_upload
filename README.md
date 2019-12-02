# file_update_upload
Upload/Update file from excel spreedsheets to GigaDB database

## Requirements and Setting
JDK 1.7 and import all jars in the lib directory.

``` /configuration/format-extension.xml ``` the xml file contains the file format and extensions mapping

``` /configuration/setting.xml ``` the xml file contains database configuration, dataset DOI, FTP prefix, excel spreadsheets location etc.

e.g.
```
<setting>
	<databaseUrl>jdbc:postgresql://localhost:5432/gigadb_v3/</databaseUrl>
	<databaseUserName>gigadb_jesse</databaseUserName>
	<databasePassword>gigadb2013</databasePassword>
	<doi>100658</doi>
	<path>./files/filev3.xls</path>
	<prefix>ftp://parrot.genomics.cn/gigadb/pub/10.5524/100001_101000/</prefix>
</setting>
```

Update the doi and filev3.xls from curators, then run the scripts. It will remove and insert the file, file_attribute and file_sample tables.
