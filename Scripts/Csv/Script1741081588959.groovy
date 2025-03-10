import java.text.SimpleDateFormat


def allData = loadCsvData("/Users/ythuynh/Katalon Studio/iPipeline_OnBoarding/Data Files/Employee.csv")
def exchangeRateData = loadCsvData("/Users/ythuynh/Katalon Studio/iPipeline_OnBoarding/Data Files/ExchangeRate.csv")

def vndExchangeRate = getExchangeRateForVND(exchangeRateData)

//CSV
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

// Load CSV data from file
def loadCsvData(String filePath) {
	def csvFile = new File(filePath)
	if (!csvFile.exists()) throw new FileNotFoundException("File not found: $filePath")
	
	def lines = csvFile.readLines()
	def headers = lines[0].split(',')
	
	def data = lines.tail().collect { line ->
		def values = line.split(',')
		[headers, values].transpose().collectEntries { [(it[0]): it[1]?.trim()] }
	}
	return data
}


def getExchangeRateForVND(def csvData) {
	Integer vndRates
	csvData.each { row ->
		def rate = row['VND']?.replaceAll(',', '')?.trim()
		if (rate?.isNumber()) {
			vndRates = rate.toInteger()
		}
	}
	return vndRates
}

def filterSalaryByName(def csvData, String name) {
	def filteredData = csvData.find { it['Name'].toLowerCase().contains(name.toLowerCase()) }
	return filteredData ? filteredData['Salary'] : null
}

def filterPeopleWithSpecificSalary(def csvData, int minSalary) {
	return csvData.findAll { person ->
		def salaryStr = person['Salary']?.replaceAll('[$,]', '')?.trim()
		if (salaryStr && salaryStr.isNumber()) {
			def salary = salaryStr.toInteger()
			return salary > minSalary
		}
		return false
	}
}

def filterFirstThreePeopleInCity(def csvData, String city) {
	return csvData.findAll { person ->
		person['Office']?.equalsIgnoreCase(city)
	}.take(3)
}

def filterPeopleLessThanAge(def csvData, int minAge) {
	return csvData.findAll { person ->
		def ageStr = person['Age']?.trim()
		if (ageStr && ageStr.isNumber()) {
			def age = ageStr.toInteger()
			return age < minAge
		}
		return false
	}
}

def filterPeopleWithNumberInAge(def csvData, String age) {
	return csvData.findAll { person ->
		def ageStr = person['Age']?.trim()
		ageStr?.contains(age)
	}
}

def filterPeopleByStartDate(def csvData, String minDate) {
	def dateFormat = new SimpleDateFormat('MM/dd/yyyy')
	def thresholdDate = dateFormat.parse(minDate)

	return csvData.findAll { person ->
		def startDateStr = person['Start date']?.trim()
		if (startDateStr) {
			def startDate = dateFormat.parse(startDateStr)
			return startDate >= thresholdDate
		}
		return false
	}
}

def filterPeopleByPositionAndSalary(def csvData, double vndExchangeRate) {
	return csvData.findAll { person ->
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

