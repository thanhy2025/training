import java.text.SimpleDateFormat

import groovy.json.JsonSlurper



def allData = loadJsonData("/Users/ythuynh/Katalon Studio/iPipeline_OnBoarding/Data Files/Employee.json")
def exchangeRateData = loadJsonData("/Users/ythuynh/Katalon Studio/iPipeline_OnBoarding/Data Files/ExchangeRate.json")

def vndExchangeRate = getExchangeRateForVND(exchangeRateData)

//JSON
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

writeJsonToCsv(allData,"/Users/ythuynh/Katalon Studio/iPipeline_OnBoarding/Data Files/Employee.csv")
writeJsonToCsv(exchangeRateData,"/Users/ythuynh/Katalon Studio/iPipeline_OnBoarding/Data Files/ExchangeRate.csv")

def loadJsonData(String filePath) {
	def jsonFile = new File(filePath)
	if (!jsonFile.exists()) throw new FileNotFoundException("File not found: $filePath")
	return new JsonSlurper().parse(jsonFile)
}

def getExchangeRateForVND(def jsonData) {
	Integer vndRates
	jsonData.each { row ->
		def rate = row['VND']?.replaceAll(',', '')?.trim()
		if (rate?.isNumber()) {
			vndRates = rate.toInteger()
		}
	}
	return vndRates
}

def filterSalaryByName(def jsonData, String name) {
	def filteredData = jsonData.find { it['Name'].toLowerCase().contains(name.toLowerCase()) }
	return filteredData ? filteredData['Salary'] : null
}

def filterPeopleWithSpecificSalary(def jsonData, int minSalary) {
	return jsonData.findAll { person ->
		def salaryStr = person['Salary']?.replaceAll('[$,]', '')?.trim()
		if (salaryStr && salaryStr.isNumber()) {
			def salary = salaryStr.toInteger()
			return salary > minSalary
		}
		return false
	}
}

def filterFirstThreePeopleInCity(def jsonData, String city) {
	return jsonData.findAll { person ->
		person['Office']?.equalsIgnoreCase(city)
	}.take(3)
}

def filterPeopleLessThanAge(def jsonData, int minAge) {
	return jsonData.findAll { person ->
		def ageStr = person['Age']?.trim()
		if (ageStr && ageStr.isNumber()) {
			def age = ageStr.toInteger()
			return age < minAge
		}
		return false
	}
}

def filterPeopleWithNumberInAge(def jsonData, String age) {
	return jsonData.findAll { person ->
		def ageStr = person['Age']?.trim()
		ageStr?.contains(age)
	}
}

def filterPeopleByStartDate(def jsonData, String minDate) {
	def dateFormat = new SimpleDateFormat('MM/dd/yyyy')
	def thresholdDate = dateFormat.parse(minDate)

	return jsonData.findAll { person ->
		def startDateStr = person['Start date']?.trim()
		if (startDateStr) {
			def startDate = dateFormat.parse(startDateStr)
			return startDate >= thresholdDate
		}
		return false
	}
}

def filterPeopleByPositionAndSalary(def jsonData, double vndExchangeRate) {
	return jsonData.findAll { person ->
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

def writeJsonToCsv(def jsonData, String filePath) {
	def csvFile = new File(filePath)
	if (!jsonData || jsonData.isEmpty()) {
		println("No data to write to CSV.")
		return
	}
	
	def headers = jsonData[0].keySet().join(',')
	def rows = jsonData.collect { row ->
		row.values().collect { it?.toString()?.replaceAll(',', '') ?: '' }.join(',')
	}
	
	csvFile.text = headers + '\n' + rows.join('\n')
	println("CSV data written to: $filePath")
}
