pipeline {
    agent any
    stages {
       stage('Compile and test') {
            steps {
		   withMaven(maven: 'maven3', mavenSettingsConfig: '00e92796-3fa4-4c0f-b4ee-fa441532f2f0', jdk: 'JDK14') {
	                 sh 'mvn -U -B clean verify install'
                    }
            }
	    post {
                always {
                    jiraSendBuildInfo site: 'pilarhu.atlassian.net'
                }
            }
        }
        stage('Sonar') {
            when { branch 'master' }
            steps {
                withSonarQubeEnv('Pilar Sonar') {
		   withMaven(maven: 'maven3', mavenSettingsConfig: '00e92796-3fa4-4c0f-b4ee-fa441532f2f0', jdk: 'JDK14') {
	                 sh 'mvn sonar:sonar'
                    }
                }
            }
        }
        stage('Nexus deploy') {
            when { branch 'master' }
            steps {
    	        withMaven(maven: 'maven3', mavenSettingsConfig: '00e92796-3fa4-4c0f-b4ee-fa441532f2f0', jdk: 'JDK14') {
	                 sh 'mvn jar:jar deploy:deploy'
                }
            }
        }
    }
}
