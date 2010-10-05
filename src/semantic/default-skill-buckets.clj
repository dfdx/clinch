
(ns semantic.default-skill-buckets
  (:use [semantic classifier linkedin]))

(defn backend-dev-buckets [index vm]
  (let [b (indexed-bucket index vm "Back-end Development" "backend")]
    (b 'install (indexed-bucket index vm "Java"
				"Java JVM JDK JSF JSP Struts
                                 Hibernate JDO"))
    (b 'install (indexed-bucket index vm "PHP"
				 "PHP Joomla Drupal Wordpress"))
    (b 'install (indexed-bucket index vm "Python"
				 "Python Django DjangoCMS Plone PyLucid"))
    (b 'install (indexed-bucket index vm "Ruby on Rails"
				"Ruby Rails rubyonrails"))
    (b 'install (indexed-bucket index vm "Perl" "Perl"))
    (b 'install (indexed-bucket index vm "Pascal" "Pascal"))
    (b 'install (indexed-bucket index vm "Delphi" "Delphi"))
    (b 'install (indexed-bucket index vm "Lisp"
				"Lisp SBCL CMUCL CommonLisp"))
    (b 'install (indexed-bucket index vm "Scheme" "Scheme"))
    (b 'install (indexed-bucket index vm "Clojure" "Clojure"))
    (b 'install (indexed-bucket index vm "Lua" "Lua"))
    (b 'install (indexed-bucket index vm "PowerShell" "PowerShell"))
    (b 'install (indexed-bucket index vm "COBOL" "COBOL"))
    (b 'install (indexed-bucket index vm "Ada" "Ada"))
    (b 'install (indexed-bucket index vm "Fortran" "Fortran"))
    (b 'install (indexed-bucket index vm "Haskell" "Haskell"))
    (b 'install (indexed-bucket index vm "Prolog" "Prolog"))
    (b 'install (indexed-bucket index vm "Scala" "Scala"))
    (b 'install (indexed-bucket index vm "Groovy" "Groovy"))
    (b 'install (indexed-bucket index vm "J2EE" "J2EE"))
    (b 'install (indexed-bucket index vm "C++" "cplusplus"))
    (b 'install (indexed-bucket index vm "C#" "csharp"))
    (b 'install (indexed-bucket index vm "Objective-C" "objectivec"))
    (b 'install (indexed-bucket index vm "Visual Basic" "visualbasic"))
    (b 'install (indexed-bucket index vm "C Shell" "cshell"))
    (b 'install (indexed-bucket index vm "ColdFusion" "ColdFusion"))
    (b 'install (indexed-bucket index vm ".NET" "dotnet"))
    b))

(defn frontend-dev-buckets [index vm]
  (let [b (indexed-bucket index vm "Front-end Development" "frontend")]
    (b 'install (indexed-bucket index vm "JavaScript"
				"JavaScript JS jQuery"))
    (b 'install (indexed-bucket index vm "HTML"
				"HTML DHTML XHTML"))
    (b 'install (indexed-bucket index vm "ActionScript" "ActionScript"))
    (b 'install (indexed-bucket index vm "Flex" "Flex"))
    (b 'install (indexed-bucket index vm "Flash" "Flash"))
    (b 'install (indexed-bucket index vm "CSS" "CSS"))
    (b 'install (indexed-bucket index vm "AJAX" "AJAX"))
    b))

(defn databases-buckets [index vm]
  (let [b (indexed-bucket index vm "Databases" "Databases DBMS RDBMS")]
    (b 'install (indexed-bucket index vm "MySQL" "MySQL"))
    (b 'install (indexed-bucket index vm "Oracle" "Oracle"))
    (b 'install (indexed-bucket index vm "CouchDB" "CouchDB"))
    (b 'install (indexed-bucket index vm "SQL" "SQL"))
    (b 'install (indexed-bucket index vm "Foxpro" "Foxpro"))
    (b 'install (indexed-bucket index vm "xBase" "xBase"))
    (b 'install (indexed-bucket index vm "Data Architecture"
				"dataarchitecture"))
    b))

(defn databases-buckets [index vm]
  (let [b (indexed-bucket index vm "Databases" "Databases DBMS RDBMS")]
    (b 'install (indexed-bucket index vm "MySQL" "MySQL"))
    (b 'install (indexed-bucket index vm "Oracle" "Oracle"))
    (b 'install (indexed-bucket index vm "CouchDB" "CouchDB"))
    (b 'install (indexed-bucket index vm "SQL" "SQL"))
    (b 'install (indexed-bucket index vm "Foxpro" "Foxpro"))
    (b 'install (indexed-bucket index vm "xBase" "xBase"))
    b))

(defn system-administration-buckets [index vm]
  (let [b (indexed-bucket index vm "System Administration"
			  "systemadministration")]
    (b 'install (indexed-bucket index vm "Unix" "unix"))
    (b 'install (indexed-bucket index vm "Linux" "Linux"))
    (b 'install (indexed-bucket index vm "Windows" "Windows"))
    (b 'install (indexed-bucket index vm "Apache" "apache"))
    (b 'install (indexed-bucket index vm "tomcat" "tomcat"))
    b))

(defn ai-buckets [index vm]
  (let [b (indexed-bucket index vm "Artificial Intelligence"
			  "Artificial Intelligence artificialintelligence AI")]
    (b 'install (indexed-bucket index vm "Data Mining" "datamining mining"))
    (b 'install (indexed-bucket index vm "Semantics" "Semantics"))
    (b 'install (indexed-bucket index vm "Machine Learning"
				"machinelearning ML"))
    (b 'install (indexed-bucket index vm "Computer Vision"
				"computervision"))
    (b 'install (indexed-bucket index vm "Recommendation Engines"
				"recommendationengine"))
    (b 'install (indexed-bucket index vm "Search Algorithms"
				"searchalgorithm"))
    b))

(defn statistics-buckets [index vm]
  (let [b (indexed-bucket index vm "Statistics" "Statistics")]
    (b 'install (indexed-bucket index vm "Matlab" "Matlab"))
    (b 'install (indexed-bucket index vm "Stata" "Stata"))
    (b 'install (indexed-bucket index vm "SPSS" "SPSS"))
    b))

(defn algorithms-buckets [index vm]
  (let [b (indexed-bucket index vm "Algorithms" "Algorithms")]
    (b 'install (indexed-bucket index vm "MapReduce" "mapreduce"))
    (b 'install (indexed-bucket index vm "Cloud Computing"
				"cloud cloudcomputing"))
    b))

(defn software-engineering-buckets [index vm]
  (let [b (indexed-bucket index vm "Software Engineering"
			  "softwareengineer softwaredeveloper
                           softwarearchitect computerscience computer")]
    (b 'install (frontend-dev-buckets index vm))
    (b 'install (backend-dev-buckets index vm))
    (b 'install (databases-buckets index vm))
    (b 'install (ai-buckets index vm))
    (b 'install (statistics-buckets index vm))
    (b 'install (system-administration-buckets index vm))
    (b 'install (algorithms-buckets index vm))
    (b 'install (indexed-bucket index vm "Encryption" "Encryption Crypto"))
    (b 'install (indexed-bucket index vm "SAS" "SAS"))
    (b 'install (indexed-bucket index vm "PowerShell" "PowerShell"))
    (b 'install (indexed-bucket index vm "ABAP" "ABAP"))
    (b 'install (bucket vm "Apache" "apache")) ;; duplicated in sysadmins
    (b 'install (bucket vm "tomcat" "tomcat")) ;; duplicated in sysadmins
    (b 'install (indexed-bucket index vm "SAP" "SAP"))
    (b 'install (indexed-bucket index vm "Cisco" "Cisco"))
    (b 'install (indexed-bucket index vm "LAMP" "LAMP"))
    (b 'install (indexed-bucket index vm "PeopleSoft" "PeopleSoft"))
    (b 'install (indexed-bucket index vm "XML" "XML"))
    (b 'install (indexed-bucket index vm "Siebel" "Siebel"))
    (b 'install (indexed-bucket index vm "Hadoop" "Hadoop"))
    (b 'install (indexed-bucket index vm "Facebook API" "facebookapi"))
    (b 'install (indexed-bucket index vm "linkedIn API" "linkedinapi"))
    (b 'install (indexed-bucket index vm "Human Computer Interaction"
				"humancomputerinteraction"))
    (b 'install (indexed-bucket index vm "Symbian" "Symbian"))
    (b 'install (indexed-bucket index vm "Gaming" "Gaming"))
    b))

(defn hardware-buckets [index vm]
  (let [b (indexed-bucket index vm "Hardware" "Hardware")]
    (b 'install (indexed-bucket index vm "Semiconductors"
				"Semiconductors"))
    b))

(defn web-design-buckets [index vm]
  (let [b (indexed-bucket index vm "Web Design" "webdesign")]
    (b 'install (indexed-bucket index vm "UI"
				"UI UX userinterface userexperience
                                 usability wireframeing"))
    (b 'install (indexed-bucket index vm "Usability"
				"usability"))
    (b 'install (indexed-bucket index vm "Wireframing"
				"wireframing"))
    b))

(defn graphical-design-buckets [index vm]
  (let [b (indexed-bucket index vm "Graphic design" "graphicdesign")]
    (b 'install (web-design-buckets index vm))
    (b 'install (indexed-bucket index vm "Photoshop" "Photoshop"))
    (b 'install (indexed-bucket index vm "Fashion" "Fashion"))
    b))

(defn finances-buckets [index vm]
  (let [b (indexed-bucket index vm "Finances" "finances")]
    (b 'install (indexed-bucket index vm "Venture capital"
				"VC Venture venturecapital"))
    (b 'install (indexed-bucket index vm "Financial advisor"
				"financialadvisor"))
    (b 'install (indexed-bucket index vm "Entrepreneur"
				"Entrepreneur Entrepreneurship startup"))
    (b 'install (indexed-bucket index vm "Investments"
				"Investments investor fundraising"))
    (b 'install (indexed-bucket index vm "Angel Investments"
				"angelinvestments angelinvestor"))
    (b 'install (indexed-bucket index vm "Financial Analysis"
				"finansialanalysis finansialanalyst"))
    (b 'install (indexed-bucket index vm "CFO"
				"cheiffinancialofficer CFO"))
    (b 'install (indexed-bucket index vm "Accounting"
				"Accounting"))
    b))

(defn hr-buckets [index vm]
  (let [b (indexed-bucket index vm "Human Resources" "HR")]
    b))

(defn community-management-buckets [index vm]
  (let [b (indexed-bucket index vm "Community Management"
			  "communitymanagement")]
    (b 'install (indexed-bucket index vm "Social Media"
				"socialmedia"))
    b))


(defn management-buckets [index vm]
  (let [b (indexed-bucket index vm "Management"
			  "Management manager Director")]
    (b 'install (hr-buckets index vm))
    (b 'install (indexed-bucket index vm "Project Management"
				"projectmanagement projectmanager"))
    (b 'install (indexed-bucket index vm "Product Management"
				"productmanagement productmanager"))
    (b 'install (indexed-bucket index vm "Community Management"
				"comunitymanagement"))
    (b 'install (indexed-bucket index vm "Recruiting" "Recruiting"))
    (b 'install (indexed-bucket index vm "Negotation" "Negotation"))
    (b 'install (indexed-bucket index vm "CEO"
				"cheifexecutiveofficer CEO"))
    b))

(defn online-marketing-buckets [index vm]
  (let [b (indexed-bucket index vm "Online Marketing" "onlinemarketing")]
    (b 'install (indexed-bucket index vm "SEO/SEM"
				"SEO SEM searchengineoptimization"))
    (b 'install (indexed-bucket index vm "Email Marketing" "emailmarketing"))
    (b 'install (indexed-bucket index vm "Virility" "virility"))
    (b 'install (indexed-bucket index vm "Online Advertising"
				"onlineadvertising"))
    b))

(defn marketing-buckets [index vm]
  (let [b (indexed-bucket index vm "Marketing" "marketing")]
    (b 'install (online-marketing-buckets index vm))
    (b 'install (indexed-bucket index vm "Sales" "Sales"))
    (b 'install (indexed-bucket index vm "Advertising" "advertising"))
    b))


(defn business-buckets [index vm]
  (let [b (indexed-bucket index vm "Business" "business president founder")]
    (b 'install (finances-buckets index vm))
    (b 'install (marketing-buckets index vm))
    (b 'install (management-buckets index vm))
    ;;(b 'install (indexed-bucket index vm "Food Industry"
    ;;   			"meal coffee dinner"))
    (b 'install (indexed-bucket index vm "Economics"
				"economy economics"))
    (b 'install (indexed-bucket index vm "Consulting"
				"Consulting"))
    (b 'install (indexed-bucket index vm "Ad Real Sate"
				"adrealsate realtor"))
    b))

(defn public-policy-buckets [index vm]
  (let [b (indexed-bucket index vm "Public Policy" "publicpolicy")]
    (b 'install (indexed-bucket index vm "NGO" "NGO"))
    (b 'install (indexed-bucket index vm "Environment" "Environment"))
    (b 'install (indexed-bucket index vm "Renewable Energy" "renewableenergy"))
    b))

(defn law-buckets [index vm]
  (let [b (indexed-bucket index vm "Law" "Law lawyer")]
    (b 'install (indexed-bucket index vm "Corporate Law" "corporatelaw"))
    (b 'install (indexed-bucket index vm "Divorce Lawyer" "divorcelawyer"))
    (b 'install (indexed-bucket index vm "Startup Lawyer" "startuplawyer"))
    b))

(defn government-buckets [index vm]
  (let [b (indexed-bucket index vm "Government" "Government")]
    (b 'install (indexed-bucket index vm "Politics" "Politics"))
    (b 'install (indexed-bucket index vm "Political Science"
				"politicalscience"))
    (b 'install (public-policy-buckets index vm))
    (b 'install (public-policy-buckets index vm))
    b))

(defn medicine-buckets [index vm]
  (let [b (indexed-bucket index vm "Medicine" "medicine medical health")]
    (b 'install (indexed-bucket index vm "Surgery" "surgery"))
    (b 'install (indexed-bucket index vm "Cardiology" "cardiology"))
    b))

(defn writing-buckets [index vm]
  (let [b (indexed-bucket index vm "Writer" "writer")]
    (b 'install (indexed-bucket index vm "Editor" "editor"))
    (b 'install (indexed-bucket index vm "Reporter" "reporter"))
    b))

(defn art-buckets [index vm]
  (let [b (indexed-bucket index vm "Artist" "artist")]
    (b 'install (indexed-bucket index vm "Photographer"
				"photographer photography"))
    b))

(defn make-root-bucket [index vm]
  (let [root (bucket vm "Root" "root")]
    (root 'install (software-engineering-buckets index vm))
    (root 'install (graphical-design-buckets index vm))
    (root 'install (business-buckets index vm))
    (root 'install (government-buckets index vm))
    (root 'install (medicine-buckets index vm))
    (root 'install (writing-buckets index vm))
    (root 'install (art-buckets index vm))
    (root 'install (hardware-buckets index vm))
    (root 'install (law-buckets index vm))
    root))
