node("master") {
    def server
    def buildInfo

    stage("SCM"){
        // Clone the code from github:
        git url :'git@github.com:slash-l/app-conan.git' , branch : "main"
    }

    stage("Artifactory Configure"){
        // Obtain an Artifactory server instance, defined in Jenkins --> Manage Jenkins --> Configure System:
        server = Artifactory.server 'JFrogChina-Server'

        // Create a local build-info instance:
        buildInfo = Artifactory.newBuildInfo()

        // Create a conan client instance:
        // conanClient = Artifactory.newConanClient()
        conanClient = Artifactory.newConanClient userHome: "/Users/jingyil"

        conanClient.run(command: "config set general.revisions_enabled=True")
        
    }

    stage("Conan build"){
        dir("conan_timer_install"){
            sh "mkdir -p build"
            sh "cd build"

            dir("build"){
                def ls = sh returnStdout: true ,script: "ls"
                echo ls

                // Run a conan build. The 'buildInfo' instance is passed as an argument to the 'run' method:
                // conanClient.run(command: "install .. --build missing", buildInfo: buildInfo)
                sh "conan install .. --build missing"
                sh "cmake .."
                sh "cmake --build ."    
            }
            
        }
    }

    stage("Upload artifacts"){
        dir("conan_timer_install/build"){
            def uploadSpec = """{
                "files": [
                    {
                        "pattern": "bin/timer",
                        "target": "slash-generic-local/conan_timer_install/v1.0.0/"
                    }
                ]
            }"""
            server.upload spec: uploadSpec 
        }
    }

    stage("Set Props"){
        def setPropsSpec = """{
            "files": [
                {
                    "pattern": "slash-generic-local/conan_timer_install/v1.0.0/timer"
                }
            ]
        }"""
        server.setProps spec: setPropsSpec, props: "SITTest=Passed"
    }

}
