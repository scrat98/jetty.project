# DO NOT EDIT - See: https://www.eclipse.org/jetty/documentation/current/startup-modules.html

[description]
Download and install some Demo Mock Resources

[environment]
ee9

[tags]
demo

[depends]
jdbc
ee9-annotations

[files]
maven://org.eclipse.jetty.ee9.demos/ee9-demo-mock-resources/${jetty.version}/jar|lib/ext/ee9-demo-mock-resources-${jetty.version}.jar