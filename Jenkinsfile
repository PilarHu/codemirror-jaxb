pipeline {
  options {
    buildDiscarder(logRotator(numToKeepStr: '100', artifactNumToKeepStr: '10'))
  }

  agent any
    stages {
      stage('Compile and test') {
        when { not { branch 'master' } }
        steps {
		  withMaven(maven: 'maven3', mavenSettingsConfig: '00e92796-3fa4-4c0f-b4ee-fa441532f2f0', jdk: 'JDK17') {
	        sh 'mvn -B clean verify'
          }
        }
	    post {
         always {
           jiraSendBuildInfo site: 'pilarhu.atlassian.net'
         }
        }
      }
      stage('Compile, test master') {
        when { branch 'master' }
        steps {
       	    withMaven(maven: 'maven3', mavenSettingsConfig: '00e92796-3fa4-4c0f-b4ee-fa441532f2f0', jdk: 'JDK17') {
	          sh 'mvn -B clean verify install'
            }
        }
	    post {
         always {
           jiraSendBuildInfo site: 'pilarhu.atlassian.net'
         }
        }
      }
    }
}
