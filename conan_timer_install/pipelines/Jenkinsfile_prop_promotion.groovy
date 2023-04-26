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
            // Add a new repository named 'conan-local' to the conan client.
            // The 'remote.add' method returns a 'serverName' string, which is used later in the script:
            // String resolveRepo = conanClient.remote.add server: server, repo: "slash-conan-remote"
            
            sh "mkdir -p build"
            sh "cd build"
            // Run a conan build. The 'buildInfo' instance is passed as an argument to the 'run' method:
            // conanClient.run(command: "install .. --build missing", buildInfo: buildInfo)
            def result = sh returnStdout: true ,script: "pwd"
            echo result

            def result2 = sh returnStdout: true ,script: "ls"
            echo result2

            sh "conan install .. --build missing"
            sh "cmake .."
            sh "cmake --build ."
        }
    }

    stage("Upload artifacts"){
        dir("conan_timer_install/build"){
            def result = sh returnStdout: true ,script: "ls"
            echo result
            
            def uploadSpec = """{
                "files": [
                    {
                        "pattern": "bin/timer",
                        "target": "slash-generic-local/conan_timer_install/v1.0.0"
                    }
                ]
            }"""
            server.upload spec: uploadSpec 
        }
    }

    // stage("Set Props"){
    //     def setPropsSpec = """{
    //         "files": [
    //             {
    //                 "pattern": "slash-generic-local/conan_timer_install/v1.0.0/timer"
    //             }
    //         ]
    //     }"""
    //     server.setProps spec: setPropsSpec, props: “SITTest=true”
    // }

}
