pipeline {
    agent any

    tools {
        jdk 'java_21'
    }

    environment {
        JAR_PATTERN = 'GigaChat-*.jar'
        REMOTE_USER = 'root'
        REMOTE_HOST = 'vmi2534046'
        REMOTE_DIR = '/home/gigachat'
        SSH_CRED_ID = '1c5d625b-c431-471e-8a91-fde82d4b4b0b'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean install -DskipTests'
            }
        }

        stage('Copy JAR to Server') {
            steps {
                sshagent (credentials: [env.SSH_CRED_ID]) {
                    sh '''
                        JAR_FILE=$(ls target/${JAR_PATTERN} | sort | tail -n 1)
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
