java -cp h2-1.1.117.jar org.h2.tools.RunScript -url 'jdbc:h2:file:hise-h2-db;DB_CLOSE_ON_EXIT=false' -user sa -showResults -script addUsers.sql
