pipeline{
    agent{
        any
    }
    tools{
        jdk 'Java17'
        maven 'Maven3'
    }
    stages{
        stage("Clean up workspace"){
            steps{
               cleanWs()
            }

        }
         stage("Check up workspace"){
            steps{
               git branch: 'main', credentials: 'github', url: 'https://github.com/Khmoussi/EasyApply.git'
            }

        }
    }
}