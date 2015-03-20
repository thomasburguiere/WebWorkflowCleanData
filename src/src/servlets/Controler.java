/**
 * src.servlets
 * LaunchWorkflow
 * TODO
 */
package src.servlets;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;

import src.model.TreatmentData;

/**
 * src.servlets
 * 
 * LaunchWorkflow
 */

@WebServlet("/upload")
@MultipartConfig
public class Controler extends HttpServlet {
    
    private String DIRECTORY_PATH = "/home/mhachet/workspace/WebWorkflowCleanData/"; 

    private boolean synonyms = false;
    private boolean tdwg4Code = false;
    private boolean raster = false;

    private ArrayList<File> inputFilesList;
    private ArrayList<File> rasterFilesList;
    private ArrayList<File> headerRasterList;
    private ArrayList<File> synonymFilesList;
    
    private TreatmentData dataTreatment;
    
    public Controler(){
	
    }
    
    
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
	this.dataTreatment = new TreatmentData();
	this.dataTreatment.generateRandomKey();
	
	List<FileItem> listFileItems = this.initiliaseRequest(request);
	System.out.println(listFileItems);
	this.initialiseInputFiles(listFileItems, response);
	
	boolean inputFilesIsValid = this.isValidInputFiles();
	if(inputFilesIsValid){
	    
	    this.launchWorkflow();
	}
	

	if(this.synonyms){
	    
	   boolean synonymFileIsValid = this.isValidSynonymFile();
	   this.launchSynonymOption(synonymFileIsValid);
	}
	
	
	if(this.tdwg4Code){
	    dataTreatment.checkIsoTdwgCode();
	}
		
	if(this.raster){
	    
	    boolean rasterFilesIsValid = this.isValidRasterFiles();
	    if(rasterFilesIsValid){
		this.launchRasterOption();
	    }
	}

    }
    
    public List<FileItem> initiliaseRequest(HttpServletRequest request){

	// on prépare pour l'envoie par la mise en oeuvre en mémoire
	DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
	ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);
	List<FileItem> items = null;
	try {
	    items = (List<FileItem>)uploadHandler.parseRequest(request);
	} catch (FileUploadException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	
	return items;
    }
    
    public void initialiseInputFiles(List<FileItem> items, HttpServletResponse response) throws IOException{
	
	response.setContentType("text/html");
	
	this.inputFilesList = new ArrayList<>();
	this.rasterFilesList = new ArrayList<>();
	this.headerRasterList = new ArrayList<>();
	this.synonymFilesList = new ArrayList<>();
	
	Iterator<FileItem> iterator = (Iterator<FileItem>)items.iterator();
	int nbFilesInput = 0;
	int nbFilesRaster = 0;
	int nbFilesHeader = 0;
	int nbFilesSynonyms = 0;
	
	if(!new File(DIRECTORY_PATH + "temp/").exists()){
	    new File(DIRECTORY_PATH + "temp/").mkdirs();
	}
	if(!new File(DIRECTORY_PATH + "temp/data/").exists()){
	    new File(DIRECTORY_PATH + "temp/data/").mkdirs();
	}

	while (iterator.hasNext()) {
	    DiskFileItem item = (DiskFileItem) iterator.next();
	    String input = "inp" + nbFilesInput;
	    String raster = "raster" + nbFilesRaster;
	    String headerRaster = "header" + nbFilesHeader;
	    String synonyms = "synonyms";

	    //System.out.println(item);

	    
	    if(item.getFieldName().equals(input)){
		System.out.println("if input : " + item);
		String fileExtensionName = item.getName();
		fileExtensionName = FilenameUtils.getExtension(fileExtensionName);
		String fileName = item.getStoreLocation().getName();
		File file = new File(DIRECTORY_PATH + "temp/data/" + fileName + "." + fileExtensionName);
		try {
		    item.write(file);
		} catch (Exception e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		this.inputFilesList.add(file);
		nbFilesInput =+ 1;
	    }
	    else if(item.getFieldName().equals(raster)){
		System.out.println("if raster : " + item);
		this.setRaster(true);
   
		String fileExtensionName = item.getName();
		fileExtensionName = FilenameUtils.getExtension(fileExtensionName);
		String fileName = item.getName();
		File file = new File(DIRECTORY_PATH + "temp/data/" + fileName);
		try {
		    item.write(file);
		} catch (Exception e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		this.rasterFilesList.add(file);
		nbFilesRaster =+ 1;
	    }
	    else if(item.getFieldName().equals(headerRaster)){
		System.out.println("if header : " + item);
   
		String fileExtensionName = item.getName();
		fileExtensionName = FilenameUtils.getExtension(fileExtensionName);
		String fileName = item.getName();
		File file = new File(DIRECTORY_PATH + "temp/data/" + fileName);
		try {
		    item.write(file);
		} catch (Exception e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		this.headerRasterList.add(file);
		nbFilesHeader =+ 1;
	    }
	    else if(item.getFieldName().equals(synonyms)){
		System.out.println("if synonym : " + item);
		this.setSynonyms(true);
		if(item.getName() != null){    
		    String fileExtensionName = item.getName();
		    fileExtensionName = FilenameUtils.getExtension(fileExtensionName);
		    String fileName = item.getStoreLocation().getName();
		    File file = new File(DIRECTORY_PATH + "temp/data/" + fileName + "." + fileExtensionName);
		    try {
			item.write(file);
		    } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		    this.synonymFilesList.add(file);
		    nbFilesSynonyms =+ 1;
		}
	    }
	    else if(item.getFieldName().equals("tdwg4")){
		this.setTdwg4Code(true);
	    }
	}

    }
    
    public void launchWorkflow() throws IOException{
	this.dataTreatment.deleteTables();
	for(int i = 0 ; i < this.inputFilesList.size() ; i++){
	    int idFile = i + 1;
    		
	    List<String> linesInputFile = this.dataTreatment.initialiseFile(inputFilesList.get(i), idFile);
	    File inputFileModified = this.dataTreatment.createTemporaryFile(linesInputFile, idFile);
	    String sqlInsert = this.dataTreatment.createSQLInsert(inputFileModified, linesInputFile);
	    this.dataTreatment.createTableDarwinCoreInput(sqlInsert);
	}	
	
	this.dataTreatment.deleteWrongIso2();
	this.dataTreatment.createTableClean();
	File wrongCoordinatesFile = dataTreatment.deleteWrongCoordinates();
	File wrongGeospatial = dataTreatment.deleteWrongGeospatial();
	
	this.dataTreatment.getPolygonTreatment();
	
    }
       
    
    public boolean isValidInputFiles(){
	System.out.println("size input : " + this.inputFilesList.size());
	 if(this.inputFilesList.size() != 0){
	     System.out.println("Your data are valid");
	     return true;
	 }
	 else{
	     System.out.println("Your data aren't valid");
	     return false;
	 }
	 
    }
    
    public boolean isValidRasterFiles(){
	System.out.println("size raster : " + this.rasterFilesList.size());
	System.out.println("size header : " + this.headerRasterList.size());
	if(this.rasterFilesList.size() == this.headerRasterList.size()){
	    if(this.rasterFilesList.size() != 0){
		return true;
	    }
	    else{
		System.err.println("You have to put a raster file (format : bil, ...) if you desire to match your point and cells data.");
		return false;
	    }
	}
	else{
	    System.err.println("You have to put a raster file AND its header file (format : hdr).");
	    return false;
	}
	
    }
    
    public boolean isValidSynonymFile(){
	System.out.println("size synonym : " + this.synonymFilesList);
	 if(this.synonymFilesList.size() != 0){
	     return true;
	 }
	 else{
	     return false;
	 }
    }
    
    public void launchSynonymOption(boolean isValidSynonymFile){
	if(isValidSynonymFile){
	    this.dataTreatment.includeSynonyms(this.synonymFilesList.get(0));
	}
	else{
	    this.dataTreatment.includeSynonyms(null);
	}
    }
    
    public void launchRasterOption(){

	File matrixFileValidCells = this.dataTreatment.checkWorldClimCell(this.rasterFilesList);

    }

    public boolean isSynonyms() {
	return synonyms;
    }

    public void setSynonyms(boolean synonyms) {
	this.synonyms = synonyms;
    }

    public boolean isTdwg4Code() {
	return tdwg4Code;
    }

    public void setTdwg4Code(boolean tdwg4Code) {
	this.tdwg4Code = tdwg4Code;
    }

    public boolean isRaster() {
	return raster;
    }

    public void setRaster(boolean raster) {
	this.raster = raster;
    }
 
}
