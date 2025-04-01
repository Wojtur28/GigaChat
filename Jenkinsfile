pipeline {
    agent any

    environment {
        JAR_PATTERN = 'GigaChat-*.jar'
        REMOTE_USER = 'root'
        REMOTE_HOST = 'vmi2534046'
        REMOTE_DIR = '/home/gigachat'
        SSH_CRED_ID = 'server_jenkins_private_key'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh './gradlew clean build'
            }
        }

        stage('Copy JAR to Server') {
            steps {
                sshagent (credentials: [env.SSH_CRED_ID]) {
                    sh '''
                        JAR_FILE=$(ls build/libs/${JAR_PATTERN} | sort | tail -n 1)
                        scp "$JAR_FILE" "$REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR/"
                    '''
                }
            }
        }

        stage('Run Deploy Script on Server') {
            steps {
                sshagent (credentials: [env.SSH_CRED_ID]) {
                    sh "ssh $REMOTE_USER@$REMOTE_HOST 'bash $REMOTE_DIR/deploy.sh'"
                }
            }
        }
    }
}
