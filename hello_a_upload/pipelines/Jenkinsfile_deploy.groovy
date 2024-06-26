node("master") {
    def server
    def buildInfo
    def conanClient

    stage("SCM"){
        // Clone the code from github:
        git url :'git@github.com:slash-l/app-conan.git' , branch : "main"
        sh "sed -i \"\" \"s#BUILD_NUMBER#${BUILD_NUMBER}#g\" ./hello_a_upload/conanfile.py"
    }

    stage("Artifactory Configure"){
        // Obtain an Artifactory server instance, defined in Jenkins --> Manage Jenkins --> Configure System:
        server = Artifactory.server 'JFrogChina-Server'

        // Create a local build-info instance:
        buildInfo = Artifactory.newBuildInfo()

        // Create a conan client instance:
        conanClient = Artifactory.newConanClient()
        // conanClient = Artifactory.newConanClient userHome: "/Users/jingyil"

        conanClient.run(command: "config set general.revisions_enabled=True")
        
    }

    stage("Conan package"){
        dir("hello_a_upload"){
            // Add a new repository named 'conan-local' to the conan client.
            // The 'remote.add' method returns a 'serverName' string, which is used later in the script:
            // String resolveRepo = conanClient.remote.add server: server, repo: "slash-conan-virtual", force: true
            // conanClient.run(command: "user -p cmVmdGtuOjAxOjE3MTAzMzEwNDM6OUdSZGI0NmZMWTVKU0R0UWpDYmRvSzBxNzZE -r ${resolveRepo} slash")

            // Run a conan build. The 'buildInfo' instance is passed as an argument to the 'run' method:
            // String command = "install . -r ${resolveRepo} --build missing"
            // conanClient.run(command: command, buildInfo: buildInfo)
            
            String command = "export-pkg . hello_a/0.1.${BUILD_NUMBER}@slash/testing --force"
            def buildInfo1 = conanClient.run command: command
            conanClient.run command: command, buildInfo: buildInfo1
            // conanClient.run(command: "export-pkg . hello_a/0.1.${BUILD_NUMBER}@slash/testing", buildInfo: buildInfo)
        }
    }

    stage("Publish Dev Local Repo"){
        String deployRepo = conanClient.remote.add server: server, repo: "slash-conan-virtual", force: true

        // Create an upload command. The 'deployRepo' string is used as a conan 'remote', so that
        // the artifacts are uploaded into it:
        String command = "upload hello_a/0.1.${BUILD_NUMBER}@slash/testing -r ${deployRepo} --all --confirm"
        // String command = "upload hello_a/0.1@user/testing -r ${deployRepo} --all --confirm"

        // Run the upload command, with the same build-info instance as an argument:
        conanClient.run(command: command, buildInfo: buildInfo)

        // Publish the build-info to Artifactory:
        server.publishBuildInfo buildInfo
    }

    stage('xray scan'){
       def scanConfig = [
               'buildName': buildInfo.name, //构建名称
               'buildNumber': buildInfo.number, //构建号
               'failBuild': true
       ]
       def scanResult = server.xrayScan scanConfig
//        echo "scanResult:" + scanResult;
   }


   stage("Promotion"){
       promotionConfig = [
           //Mandatory parameters
           'buildName'          : buildInfo.name,
           'buildNumber'        : buildInfo.number,
           'targetRepo'         : 'slash-conan-test-local',


           //Optional parameters
           'comment'            : 'this is the promotion comment',
           'sourceRepo'         : 'slash-conan-dev-local',
           'status'             : 'Released',
           'includeDependencies': true,
           'failFast'           : true,
           'copy'               : true
       ]


       // Promote build
       server.promote promotionConfig
   }

        
}