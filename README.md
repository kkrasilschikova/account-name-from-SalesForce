## account-name-from-salesforce
#### Purpose:
You may get Account name from SalesForce by providing the program with .csv file that has Case number column.  
Please prepare .csv file in advance.  
Output will be .html file in the folder where you run the program.

#### Library dependencies:
```
libraryDependencies += "com.force.api" % "force-partner-api" % "40.0.0"
libraryDependencies += "com.force.api" % "force-wsc" % "40.0.0"
```

#### In order to run the program on Windows machine

0) prepared cases.csv was put, for example, in D:\

1) check that Java is installed on your computer
```
PS C:\Users\Administrator> java
```
If not, download it from https://www.java.com/en/download/

2) download AccountNameFromSF.jar file from https://github.com/kkrasilschikova/account-name-from-salesforce/blob/master/out/artifacts/AccountNameFromSalesForce_jar/AccountNameFromSalesForce.jar, for example, to C:\temp

3) open PowerShell and specify path to .csv file, username and passwordSecurityToken (no spaces), for example,

*PS C:\Users\Administrator>*
java -jar "C:\temp\AccountNameFromSF.jar" "D:\cases.csv" "username@domain.com" "PasswordSecurityToken"

#### In order to run the program on Linux system

0) prepared cases.csv was put, for example, in home/Downloads/

1) download AccountNameFromSF.jar file from https://github.com/kkrasilschikova/account-name-from-salesforce/blob/master/out/artifacts/AccountNameFromSalesForce_jar/AccountNameFromSalesForce.jar, for example, to home/Downloads/

2) open terminal window and specify path to .csv file, username and passwordSecurityToken (no spaces), for example,

*[root@localhost]$*
java -jar home/Downloads/AccountNamefromSalesForce.jar "home/Downloads/cases.csv" "username@domain.com" 'PasswordSecurityToken'
