import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import java.text.SimpleDateFormat

import groovy.json.JsonOutput

// Get the test data object
def testData = findTestData('Employee')

def testData1 = findTestData('ExchangeRate')


def vndExchangeRate = getExchangeRateForVND(testData1)
println("VND rate: " + vndExchangeRate)

def allData = getAllExcelData(testData)
def exchangeRateAllData = getAllExcelData(testData1)

//EXCEL
//println('Collected data: ' + allData)

//Salary of Bradley
def salary = filterSalaryByName(allData, "Bradley")
println('Salary By Name Bradley: ' + salary)

//People with Salary > $400
def peopleWithSalary = filterPeopleWithSpecificSalary(allData, 400)
println('People with Salary > $400: ' + peopleWithSalary)

//First 3 people with office in Tokyo
def peopleInCity = filterFirstThreePeopleInCity(allData, "Tokyo")
println("First 3 people in Tokyo: " + peopleInCity)

//People <= 40 years old
def peopleInAges = filterPeopleLessThanAge(allData, 40)
println("People who < 40 years old: " + peopleInAges)

//People with the number 3 in their age
def peopleWith3InAge = filterPeopleWithNumberInAge(allData, "3")
println("People with 3 in age: " + peopleWith3InAge)

//People with start date from 1/1/2011 onwards
def peopleWithStartDate = filterPeopleByStartDate(allData, "01/01/2011")
println("People with startDate from 1/1/2011: " + peopleWithStartDate)

//People with position as Accountant or Software Engineer and salary < 5 million VND 
def peopleInRoleAndSalary = filterPeopleByPositionAndSalary (allData, vndExchangeRate)
println("People with Position and Salary: " + peopleInRoleAndSalary)

def jsonEmployee = convertExcelDataToJson(testData)
println("JSON Output: " + jsonEmployee)

def jsonExchangeRate = convertExcelDataToJson(testData1)
println("JSON Output: " + jsonExchangeRate)

writeJsonToFile(allData, "/Users/ythuynh/Katalon Studio/iPipeline_OnBoarding/Data Files/Employee.json")
writeJsonToFile(exchangeRateAllData, "/Users/ythuynh/Katalon Studio/iPipeline_OnBoarding/Data Files/ExchangeRate.json")

def writeJsonToFile(def jsonData, String filePath) {
	def jsonFile = new File(filePath)
	jsonFile.text = JsonOutput.prettyPrint(JsonOutput.toJson(jsonData))
	println("JSON data written to: $filePath")
}

def getExchangeRateForVND(def testData) {
    Integer vndRates;

    for (def i = 1; i <= testData.getRowNumbers(); i++) {
        def rate = testData.getValue('VND', i)?.replaceAll(',', '')?.trim()
        if (rate?.isNumber()) {
            vndRates = rate.toInteger()
        }
    }
    return vndRates
}

def getAllExcelData(def testData) {
    List<Map<String, String>> allData = []

    def columnNames = testData.getColumnNames()

    for (def i = 1; i <= testData.getRowNumbers(); i++) {
        Map<String, String> rowData = [:]

        columnNames.each({ def columnName ->
                (rowData[columnName]) = testData.getValue(columnName, i)
            })

        allData.add(rowData)
    }
    
    return allData
}

def filterSalaryByName(def allData, String name) {
    def filteredData = allData.find { it['Name'].toLowerCase().contains(name.toLowerCase()) }
    return filteredData ? filteredData['Salary'] : null
}

def filterPeopleWithSpecificSalary(def allData, int minSalary) {
    return allData.findAll { person ->
        def salaryStr = person['Salary']?.replaceAll('[$,]', '')?.trim()
        if (salaryStr && salaryStr.isNumber()) {
            def salary = salaryStr.toInteger()
            return salary > minSalary
        }
        return false
    }
}

def filterFirstThreePeopleInCity(def allData, String city) {
	return allData.findAll { person ->
		person['Office']?.equalsIgnoreCase(city)
	}.take(3)
}

def filterPeopleLessThanAge(def allData, int minAge) {
	return allData.findAll { person ->
		def ageStr = person['Age']?.trim()
		if (ageStr && ageStr.isNumber()) {
			def age = ageStr.toInteger()
			return age < minAge
		}
		return false
	}
}

def filterPeopleWithNumberInAge(def allData, String age) {
	return allData.findAll { person ->
		def ageStr = person['Age']?.trim()
		if (ageStr.contains(age)) {
			return ageStr
		}
		return false
	}
}

def filterPeopleByStartDate(def allData, String minDate) {
	def dateFormat = new SimpleDateFormat('MM/dd/yyyy')
	def thresholdDate = dateFormat.parse(minDate)

	return allData.findAll { person ->
		def startDateStr = person['Start date']?.trim()
		if (startDateStr) {
			def startDate = dateFormat.parse(startDateStr)
			return startDate >= thresholdDate
		}
		return false
	}
}

def filterPeopleByPositionAndSalary(def allData, double vndExchangeRate) {
	return allData.findAll { person ->
		def position = person['Position']?.trim()
		def salaryStr = person['Salary']?.replaceAll('[$,]', '')?.trim()

		if (position in ['Accountant', 'Software Engineer'] && salaryStr?.isNumber()) {
			def salaryUSD = salaryStr.toDouble()
			def salaryVND = salaryUSD * vndExchangeRate
			return salaryVND < 5000000 // 5 million VND
		}
		return false
	}
}

def convertExcelDataToJson(def testData) {
	List<Map<String, String>> allData = []

	def columnNames = testData.getColumnNames()

	for (def i = 1; i <= testData.getRowNumbers(); i++) {
		Map<String, String> rowData = [:]

		columnNames.each { columnName ->
			rowData[columnName] = testData.getValue(columnName, i)
		}

		allData.add(rowData)
	}

	return JsonOutput.prettyPrint(JsonOutput.toJson(allData))
}