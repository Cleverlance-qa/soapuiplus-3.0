//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//>                 					DSLooperXLS(CSV)
//>                 					version 3.0.0
//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

import com.eviware.soapui.support.XmlHolder;
import jxl.*;
import jxl.write.*;
import groovy.io.*;
import groovy.json.*;

def projRoot = context.expand('${projectDir}');
def env2test = context.expand('${#Project#env2test}');
def DS = testRunner.testCase.testSteps['DataSources'].getPropertyValue("DSInput");
def DS_type = testRunner.testCase.testSteps['DataSources'].getPropertyValue("DSInputType");
def DSPath;
def DSPath_xls;
def DSPath_csv;
def DSPath2_xls;
def DSPath2_csv;
def debug_error = context.expand('${#Project#debug_error}');
def DSPath_ctrl = "";
def DSPath2_ctrl = "";


if (DS.startsWith("DS_envConfig")) {
	
	// kontrola na pocet	
	def path = (context.expand('${projectDir}') + "/01_DataSources/").replaceAll("\\\\","/");
	def count = new File(path).listFiles().findAll { it.name ==~ /DS_envConfig.*/ }.size();
	if (debug_error == "true"){
		log.info "pocet stejnych 'DS_envConfig.*' = " + count;
	}
	if (count > 1) {
	
		log.info " ****** ****** POZOR ****** ******";
		log.error "Vstupni soubor 'DS_envConfig' nemuze byt ve vice formatech. Musi byt VZDY jen jeden tedy typ napr. XLS nebo CSV";
		log.info " ****** ****** POZOR ****** ******";
		testRunner.fail("Problem with dataSource");
		
	} else {
		
		DSPath_control = ( projRoot + "/01_DataSources/" + DS + "." + DS_type ).replaceAll("\\\\","/");
		if (debug_error == "true"){
			log.info "DSPath_control = " + DSPath_control
		}
		
		DSPath_xls = ( projRoot + "/01_DataSources/" + DS + ".xls" ).replaceAll("\\\\","/");
		DSPath_csv = ( projRoot + "/01_DataSources/" + DS + ".csv" ).replaceAll("\\\\","/");
		
		if (new File(DSPath_control).exists() == true) {
			
			if (new File(DSPath_xls).exists() == true) {
				//log.info "XLS";
				DSPath = DSPath_xls;
				DSPath_ctrl = "xls";
			}
			else if (new File(DSPath_csv).exists() == true) {
				//log.info "CSV";
				DSPath = DSPath_csv;
				DSPath_ctrl = "csv";
			}

		} 
		else {
			log.error "File CSV or XLS or XLSX not found !!!";
		}

		testRunner.testCase.setPropertyValue("DSPath", DSPath);
		if (debug_error == "true"){
			log.info "DSPath (data input file) = " + DSPath;
		}
	}
	
}
else { // libovolny nazev data souboru
	
	// kontrola na pocet
	def path = (context.expand('${projectDir}') + "/01_DataSources/").replaceAll("\\\\","/");
		if (debug_error == "true") {
		log.info "cesta k datovemu adresari = " + path;
	}
	
	def prefix = DS + "_" + env2test + "." + DS_type;
	def prefixPattern = "${prefix}*.*";
	def count = new File(path).listFiles().findAll { it.name ==~ /${prefixPattern}/ }.size();
	
	if (debug_error == "true") {
		log.info "pocet souboru '" + DS + "_" + env2test + "." + DS_type + "' = " + count;
	}
	
	if (count > 1) {
		log.info " ****** ****** POZOR ****** ******";
		log.error "Vstupni soubor '" + DS + "_" + env2test + "' nemuze byt ve vice formatech. Musi byt VZDY jen jeden tedy typ napr. XLS nebo XLSX nebo CSV !!!";
		log.info " ****** ****** POZOR ****** ******";

	} else {
		
		DSPath2_xls = ( projRoot + "/01_DataSources/" + DS + "_" + env2test + ".xls" ).replaceAll("\\\\","/");
		DSPath2_csv = ( projRoot + "/01_DataSources/" + DS + "_" + env2test + ".csv" ).replaceAll("\\\\","/");
				
		if (new File(DSPath2_xls).exists() == true) {
			//log.info "XLS 2";
			DSPath = DSPath2_xls;
			DSPath2_ctrl = "xls";
		} else if (new File(DSPath2_csv).exists() == true) {
			//log.info "CSV 2";
			DSPath = DSPath2_csv;
			DSPath2_ctrl = "csv";
		}
		else {
			log.error "File CSV or XLS or XLSX not found !!!";
		}
		
		// ulozena cesta pro data file
		testRunner.testCase.setPropertyValue("DSPath", DSPath);
		
		if (debug_error == "true") {
			log.info "DSPath (data input file pro testovaci prostredi '" + env2test + "') = " + DSPath;
		}
	}

}

// ********************* XLS soubor ****************************** //

if ( (DSPath_ctrl == "xls") || (DSPath2_ctrl == "xls") ) {
	
	if (debug_error == "true") {
		log.info "XLS soubor !!!";
	}
	
	def Workbook wb = Workbook.getWorkbook(new File(DSPath));
	def Sheet sh = wb.getSheet(0);

	def rowsCount= sh.getRows().toInteger();
	if (debug_error == "true") {
		log.info "rowsCount = " + rowsCount
	}
	
	def colsCount = sh.getColumns().toInteger();
	if (debug_error == "true") {
		log.info "colsCount = " + colsCount;
	}

	if(rowsCount == 1) {
	
		testRunner.fail("DataSource is empty");
		log.info "DataSource is empty - testCase run was canceled";
	}
	else {

		def inputProps = testRunner.testCase.getTestStepByName("InputProps");
		def looperProps = testRunner.testCase.getTestStepByName("LooperProps");
		looperProps.setPropertyValue("RowsCount", rowsCount.toString());
		actualRow = looperProps.getPropertyValue("ActualRow").toString();
		actualRow = actualRow.toInteger();
	
		def nextRow;
		if(actualRow > rowsCount-2 ) {
			
			nextRow = "1";
			testRunner.testCase.setPropertyValue("nextRow", nextRow);
		}
		else {
			
			nextRow = actualRow + 1;
			nextRow = nextRow.toString();
			testRunner.testCase.setPropertyValue("nextRow", nextRow);
		}
	
		nextRow = context.expand('${#TestCase#nextRow}');

		int i = 1;
		for(int j = 0; j < colsCount; j++) {

			headerCol = i.toString();
			//log.info headerCol;
			Cell headerCol = sh.getCell(j,0);
			header = headerCol.getContents();
			Cell cell = sh.getCell(j, actualRow);
			field = cell.getContents();
			inputProps.setPropertyValue(header, field);
			if (debug_error == "true"){ 
				log.info("header = " + header + ",value = " + field);
			}
			i++
		}
	
		wb.close();

		looperProps.setPropertyValue("ActualRow", nextRow.toString());
		nextRow++;
		log.info "DataSource = " + DS + ", actual row = ${(actualRow)} of ${(rowsCount)-1}";
		log.info "*******************************************************************************************************************";	
		looperProps.setPropertyValue("NextRow", nextRow.toString());
	
		if (actualRow == rowsCount-1) {
	
			looperProps.setPropertyValue("StopLoop", "StopLoop");
		}
		else if (actualRow==0) {
		
			def runner = new com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner(testRunner.testCase, null);
			looperProps.setPropertyValue("StopLoop", "NextLoop");
		}
		else {
	
			looperProps.setPropertyValue("StopLoop", "NextLoop");
		}
	}
}

// ********************* CSV soubor ****************************** //

if ((DSPath_ctrl == "csv") || (DSPath2_ctrl == "csv")) {
	
	if (debug_error == "true") { 
		log.info " ******************************** ";
		log.info "CSV soubor !!!"; 
		log.info "DSPath = " + DSPath;
		log.info " ******************************** ";
	}
	
	def File DSInput = new File(DSPath);
	def List DSRows = DSInput.readLines();
	
	// header CSV
	def fileHeaderLine = DSRows[0]
	if (debug_error == "true") {
		log.info "fileHeaderLine = " + fileHeaderLine;
	}
	
	//log.info "DSContent = " + DSRows;
	def size = DSRows.size.toInteger();
	if (debug_error == "true") {
		log.info "Pocet radku v souboru CSV = " + size;
	}
	if(size == 0) { 
		// prazdny nebo nenaplneny hodnotami soubor CSV
	
		testRunner.fail("DataSource is empty");
		log.info "DataSource is empty - testCase run was canceled";
	}
	else {
	
		def looperProps = testRunner.testCase.getTestStepByName("LooperProps");
		looperProps.setPropertyValue("RowsCount", size.toString());
	
		def actualRow = looperProps.getPropertyValue("ActualRow").toString();
		actualRow = actualRow.toInteger();
	
		def nextRow;
		
		if(actualRow > size-2 ) {
		
			nextRow = "1";
			testRunner.testCase.setPropertyValue("nextRow", nextRow);
		}
		else {
		
			nextRow = actualRow + 1;
			nextRow = nextRow.toString();
			testRunner.testCase.setPropertyValue("nextRow", nextRow);
		}
		
		nextRow = context.expand('${#TestCase#nextRow}');
		//log.info "nextRow = " + nextRow;
	
		def inputProps = testRunner.testCase.getTestStepByName("InputProps");
		
		// header
		def String[] items_header = fileHeaderLine.split(";");
		if (debug_error == "true"){ log.info "items_header = " + items_header; }
		
		def valuesCount_header = items_header.length;
		//log.info "valuesCount_header = " + valuesCount_header;

		// obsah
		def values = DSRows[actualRow].toString();
		def String[] items = values.split(";");
		if (debug_error == "true"){ log.info "items = " + items; }
		
		def valuesCount = items.length;
		//log.info "valuesCount = " + valuesCount;

		// values
		int i = 1;
		for(int j = 0; j < valuesCount_header; j++) {
			// header
			def value_header = fileHeaderLine.split(";")[j];
			//log.info "value_header = " + value_header;

			// items
			def value_items = values.split(";")[j];
			//log.info "value_items = " + value_items;

			//log.info "pro zapis = " + value_header + "," + value_items;
			inputProps.setPropertyValue(value_header, value_items);
			if (debug_error == "true") { 
				log.info("header = " + value_header + ",value = " + value_items);
			}
			i++
		}
	
		looperProps.setPropertyValue("ActualRow", nextRow.toString());
		nextRow++;
		log.info "DataSource = " + DS + ", actual row = ${(actualRow+1)} of $DSRows.size"; // ****
		log.info "*******************************************************************************************************************";	
		//log.info "DS Row = ${(actualRow+1)} of $DSRows.size";
	
		looperProps.setPropertyValue("NextRow", nextRow.toString());
		//log.info "Values from DSInput = " + values;
	
		if (actualRow==size-1) {
		
			looperProps.setPropertyValue("StopLoop", "StopLoop");
		}
		else if (actualRow==0) {
		
			def runner = new com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner(testRunner.testCase, null)
			looperProps.setPropertyValue("StopLoop", "NextLoop");
		}
		else {
	
			looperProps.setPropertyValue("StopLoop", "NextLoop");
		}
	}
}
