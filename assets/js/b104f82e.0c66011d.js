(self.webpackChunkjarbird_docs=self.webpackChunkjarbird_docs||[]).push([[821],{3905:function(e,t,r){"use strict";r.d(t,{Zo:function(){return p},kt:function(){return d}});var n=r(7294);function o(e,t,r){return t in e?Object.defineProperty(e,t,{value:r,enumerable:!0,configurable:!0,writable:!0}):e[t]=r,e}function a(e,t){var r=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),r.push.apply(r,n)}return r}function i(e){for(var t=1;t<arguments.length;t++){var r=null!=arguments[t]?arguments[t]:{};t%2?a(Object(r),!0).forEach((function(t){o(e,t,r[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(r)):a(Object(r)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(r,t))}))}return e}function l(e,t){if(null==e)return{};var r,n,o=function(e,t){if(null==e)return{};var r,n,o={},a=Object.keys(e);for(n=0;n<a.length;n++)r=a[n],t.indexOf(r)>=0||(o[r]=e[r]);return o}(e,t);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);for(n=0;n<a.length;n++)r=a[n],t.indexOf(r)>=0||Object.prototype.propertyIsEnumerable.call(e,r)&&(o[r]=e[r])}return o}var s=n.createContext({}),u=function(e){var t=n.useContext(s),r=t;return e&&(r="function"==typeof e?e(t):i(i({},t),e)),r},p=function(e){var t=u(e.components);return n.createElement(s.Provider,{value:t},e.children)},m={inlineCode:"code",wrapper:function(e){var t=e.children;return n.createElement(n.Fragment,{},t)}},c=n.forwardRef((function(e,t){var r=e.components,o=e.mdxType,a=e.originalType,s=e.parentName,p=l(e,["components","mdxType","originalType","parentName"]),c=u(r),d=o,h=c["".concat(s,".").concat(d)]||c[d]||m[d]||a;return r?n.createElement(h,i(i({ref:t},p),{},{components:r})):n.createElement(h,i({ref:t},p))}));function d(e,t){var r=arguments,o=t&&t.mdxType;if("string"==typeof e||o){var a=r.length,i=new Array(a);i[0]=c;var l={};for(var s in t)hasOwnProperty.call(t,s)&&(l[s]=t[s]);l.originalType=e,l.mdxType="string"==typeof e?e:o,i[1]=l;for(var u=2;u<a;u++)i[u]=r[u];return n.createElement.apply(null,i)}return n.createElement.apply(null,r)}c.displayName="MDXCreateElement"},8215:function(e,t,r){"use strict";var n=r(7294);t.Z=function(e){var t=e.children,r=e.hidden,o=e.className;return n.createElement("div",{role:"tabpanel",hidden:r,className:o},t)}},1395:function(e,t,r){"use strict";r.d(t,{Z:function(){return p}});var n=r(7294),o=r(944),a=r(6010),i="tabItem_1uMI",l="tabItemActive_2DSg";var s=37,u=39;var p=function(e){var t=e.lazy,r=e.block,p=e.defaultValue,m=e.values,c=e.groupId,d=e.className,h=(0,o.Z)(),v=h.tabGroupChoices,b=h.setTabGroupChoices,f=(0,n.useState)(p),y=f[0],g=f[1],k=n.Children.toArray(e.children),w=[];if(null!=c){var j=v[c];null!=j&&j!==y&&m.some((function(e){return e.value===j}))&&g(j)}var N=function(e){var t=e.currentTarget,r=w.indexOf(t),n=m[r].value;g(n),null!=c&&(b(c,n),setTimeout((function(){var e,r,n,o,a,i,s,u;(e=t.getBoundingClientRect(),r=e.top,n=e.left,o=e.bottom,a=e.right,i=window,s=i.innerHeight,u=i.innerWidth,r>=0&&a<=u&&o<=s&&n>=0)||(t.scrollIntoView({block:"center",behavior:"smooth"}),t.classList.add(l),setTimeout((function(){return t.classList.remove(l)}),2e3))}),150))},C=function(e){var t,r;switch(e.keyCode){case u:var n=w.indexOf(e.target)+1;r=w[n]||w[0];break;case s:var o=w.indexOf(e.target)-1;r=w[o]||w[w.length-1]}null==(t=r)||t.focus()};return n.createElement("div",{className:"tabs-container"},n.createElement("ul",{role:"tablist","aria-orientation":"horizontal",className:(0,a.Z)("tabs",{"tabs--block":r},d)},m.map((function(e){var t=e.value,r=e.label;return n.createElement("li",{role:"tab",tabIndex:y===t?0:-1,"aria-selected":y===t,className:(0,a.Z)("tabs__item",i,{"tabs__item--active":y===t}),key:t,ref:function(e){return w.push(e)},onKeyDown:C,onFocus:N,onClick:N},r)}))),t?(0,n.cloneElement)(k.filter((function(e){return e.props.value===y}))[0],{className:"margin-vert--md"}):n.createElement("div",{className:"margin-vert--md"},k.map((function(e,t){return(0,n.cloneElement)(e,{key:t,hidden:e.props.value!==y})}))))}},9443:function(e,t,r){"use strict";var n=(0,r(7294).createContext)(void 0);t.Z=n},944:function(e,t,r){"use strict";var n=r(7294),o=r(9443);t.Z=function(){var e=(0,n.useContext)(o.Z);if(null==e)throw new Error("`useUserPreferencesContext` is used outside of `Layout` Component.");return e}},938:function(e,t,r){"use strict";r.r(t),r.d(t,{frontMatter:function(){return s},metadata:function(){return u},toc:function(){return p},default:function(){return c}});var n=r(2122),o=r(9756),a=(r(7294),r(3905)),i=r(1395),l=r(8215),s={title:"Publish to custom Maven repositories",tags:"tutorial",sidebar_label:"Custom Maven repository",sidebar_position:5},u={unversionedId:"tutorials/custommaven",id:"tutorials/custommaven",isDocsHomePage:!1,title:"Publish to custom Maven repositories",description:"In this tutorial we will go through the process of publishing to a custom",source:"@site/docs/tutorials/custommaven.mdx",sourceDirName:"tutorials",slug:"/tutorials/custommaven",permalink:"/jarbird/docs/tutorials/custommaven",editUrl:"https://github.com/hkhc/jarbird-docs/docs/tutorials/custommaven.mdx",version:"current",sidebar_label:"Custom Maven repository",sidebarPosition:5,frontMatter:{title:"Publish to custom Maven repositories",tags:"tutorial",sidebar_label:"Custom Maven repository",sidebar_position:5},sidebar:"tutorialSidebar",previous:{title:"Publish to Maven Central",permalink:"/jarbird/docs/tutorials/mavencentral"},next:{title:"Publishing Android Components",permalink:"/jarbird/docs/tutorials/android"}},p=[{value:"Project setup",id:"project-setup",children:[{value:"gradle.properties",id:"gradleproperties",children:[]},{value:"build.gradle",id:"buildgradle",children:[]},{value:"pom.yaml",id:"pomyaml",children:[]},{value:"Appendix: Setup Reposilite server",id:"appendix-setup-reposilite-server",children:[]}]}],m={toc:p};function c(e){var t=e.components,r=(0,o.Z)(e,["components"]);return(0,a.kt)("wrapper",(0,n.Z)({},m,r,{components:t,mdxType:"MDXLayout"}),(0,a.kt)("p",null,"In this tutorial we will go through the process of publishing to a custom\nmaven repository."),(0,a.kt)("p",null,"The sample project is ",(0,a.kt)("a",{parentName:"p",href:"https://github.com/hkhc/jarbird-samples/tree/master/custommaven"},"here"),"."),(0,a.kt)("p",null,"You need access to your own repository manager server. For learning purpose you\nmay ",(0,a.kt)("a",{parentName:"p",href:"#reposilite"},"setup")," a simple repository manager server on your local\ncomputer to try. There are a lot of open source and commercial ",(0,a.kt)("a",{parentName:"p",href:"https://maven.apache.org/repository-management.html"},"solutions")," for\nthat, pick one you like."),(0,a.kt)("h2",{id:"project-setup"},"Project setup"),(0,a.kt)("h3",{id:"gradleproperties"},"gradle.properties"),(0,a.kt)("p",null,"Add maven server information to ",(0,a.kt)("inlineCode",{parentName:"p"},"gradle.properties"),". We can have multiple maven\nserver settings in one ",(0,a.kt)("inlineCode",{parentName:"p"},"gradle.properties"),' file. Each of the settings is\nidentified by an ID. ("demo" in the example below.). '),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-properties",metastring:'title="~/.gradle/gradle.properties"',title:'"~/.gradle/gradle.properties"'},"# ...\nrepository.maven.demo.release=http://localhost\nrepository.maven.demo.snapshot=http://localhost\nrepository.maven.demo.username=writeuser\nrepository.maven.demo.password=....token....\n# The following line is needed only if release or snapshot URL are HTTP.\nrepository.maven.demo.allowInsecureProtocol=true\n# ...\n")),(0,a.kt)("p",null,"The last line is an opt-in step to allow Jarbird plugin to use insecure\nserver, to allow release and snapshot URL to be HTTP rather than HTTPS."),(0,a.kt)("p",null,"If you are using Rosilite server as the ",(0,a.kt)("a",{parentName:"p",href:"#rosilite"},"setup"),", fill in the\ntoken for user ",(0,a.kt)("inlineCode",{parentName:"p"},"writeuser")," to ",(0,a.kt)("inlineCode",{parentName:"p"},"~/.gradle/gradle.properties"),"."),(0,a.kt)("p",null,"We need to provide a URL for release version of component, a URL for\nsnapshot version of component, username and password of your account."),(0,a.kt)("h3",{id:"buildgradle"},"build.gradle"),(0,a.kt)("p",null,"Tell Jarbird plugin that we want to publish to a custom Maven repository. We\nspecify the ID of our Maven repository setting in ",(0,a.kt)("inlineCode",{parentName:"p"},"gradle.properties"),"."),(0,a.kt)(i.Z,{defaultValue:"buildgradle-1",values:[{label:"build.gradle",value:"buildgradle-1"},{label:"build.gradle.kts",value:"buildgradlekts"}],mdxType:"Tabs"},(0,a.kt)(l.Z,{value:"buildgradle-1",mdxType:"TabItem"},(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-groovy",metastring:"{5}","{5}":!0},'// ...\njarbird {\n    pub {\n        mavenRepo("demo")\n    }\n}\n// ...\n'))),(0,a.kt)(l.Z,{value:"buildgradlekts",mdxType:"TabItem"},(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-kotlin",metastring:"{5}","{5}":!0},'// ...\njarbird {\n    pub {\n        mavenRepo("demo")\n    }\n}\n// ...\n')))),(0,a.kt)("p",null,(0,a.kt)("inlineCode",{parentName:"p"},"mavenLocal()")," can be omitted if you are not going to publish to Maven Local repository."),(0,a.kt)("h3",{id:"pomyaml"},"pom.yaml"),(0,a.kt)("p",null,"Unlike MavenCentral, we may skip some details in pom.xml. Jarbird get all of\nthose at the ",(0,a.kt)("inlineCode",{parentName:"p"},"pom.yaml")," file:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-yaml",metastring:'title="pom.yaml"',title:'"pom.yaml"'},"group: jarbirdsamples\nartifactId: mevendemo\nversion: 1.0\npackaging: jar\n\nlicenses:\n  - name: Apache-2.0\n    dist: repo\n\ndevelopers:\n  - id: demo\n    name: Jarbird Demo\n    email: jarbird.demo@fake-email.com\n\nscm:\n  repoType: github.com\n  repoName: demo/jarbird-samples/maven-demo\n\n")),(0,a.kt)("p",null,"See TODO for more details about the content of ",(0,a.kt)("inlineCode",{parentName:"p"},"pom.yaml"),"."),(0,a.kt)("p",null,"Jarbird plugin read the file and create proper POM file for publishing automatically."),(0,a.kt)("p",null,"With this setup, we can see more tasks available with ",(0,a.kt)("inlineCode",{parentName:"p"},"./gradlew tasks")),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-text"},"Jarbird publishing tasks\n------------------------\njbPublish - Publish\njbPublishCustommaven - Publish module 'custommaven' to all targeted repositories\njbPublishCustommavenToMavenDemo - Publish module 'custommaven' to Maven repository 'demo'\njbPublishCustommavenToMavenLocal - Publish module 'custommaven' to Maven Local repository\njbPublishCustommavenToMavenRepository - Publish module 'custommaven' to all Maven repositories\njbPublishToMavenDemo - Publish to Maven repository 'demo'\njbPublishToMavenLocal - Publish to Maven Local repository\njbPublishToMavenRepository - Publish to all Maven repositories\n")),(0,a.kt)("p",null,"The task ",(0,a.kt)("inlineCode",{parentName:"p"},"jbPublish")," publishes component to all specified repositories,\nwhich is MavenLocal and MavenCentral for this sample.\nWe may use ",(0,a.kt)("inlineCode",{parentName:"p"},"jbPublishToMavenDemo"),'  to publish all of our components to the\n"demo" Maven repository. '),(0,a.kt)("p",null,"If you are using Rosilite server locally, you can observe the publishing\nactivities at the console window of Rosilite, and find the published files\nat ",(0,a.kt)("inlineCode",{parentName:"p"},"repositories")),(0,a.kt)("h3",{id:"appendix-setup-reposilite-server"},"Appendix: Setup Reposilite server"),(0,a.kt)("p",null,(0,a.kt)("a",{parentName:"p",href:"https://reposilite.com/"},"Reposilite")," is a simple, open-source Maven\nrepository manager server. It can be set up in minutes on your local\ncomputer, which is ideal to learn about how Jarbird works with custom Maven\nrepositories."),(0,a.kt)("p",null,"Assumed that you have an ordinary JDK (8+) installed on your computer."),(0,a.kt)("h4",{id:"download-the-binary"},"Download the binary"),(0,a.kt)("p",null,"Download the jar ",(0,a.kt)("a",{parentName:"p",href:"https://github.com/dzikoysk/reposilite/releases"},"here"),". At\nthe time of writing, the latest version is 2.9.22. Create a directory\nlocally, and place the jar file there."),(0,a.kt)("h4",{id:"start-server"},"Start server"),(0,a.kt)("p",null,"Start the server with ",(0,a.kt)("inlineCode",{parentName:"p"},"java -jar rosilite-2.9.22.jar")),(0,a.kt)("p",null,"After it has finished start up, we may type command in the command shell\nwindow directly."),(0,a.kt)("h4",{id:"create-users-and-tokens"},"Create users and tokens"),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},"Type ",(0,a.kt)("inlineCode",{parentName:"li"},"keygen / adminuser m"),", note the token generated. If you missed that,\nyou may find it in the file ",(0,a.kt)("inlineCode",{parentName:"li"},"tokens.dat")," in the same directory."),(0,a.kt)("li",{parentName:"ul"},"Type ",(0,a.kt)("inlineCode",{parentName:"li"},"keygen / writeuser w"),", note the token generated.   ")),(0,a.kt)("h4",{id:"done"},"Done!"))}c.isMDXComponent=!0},6010:function(e,t,r){"use strict";function n(e){var t,r,o="";if("string"==typeof e||"number"==typeof e)o+=e;else if("object"==typeof e)if(Array.isArray(e))for(t=0;t<e.length;t++)e[t]&&(r=n(e[t]))&&(o&&(o+=" "),o+=r);else for(t in e)e[t]&&(o&&(o+=" "),o+=t);return o}function o(){for(var e,t,r=0,o="";r<arguments.length;)(e=arguments[r++])&&(t=n(e))&&(o&&(o+=" "),o+=t);return o}r.d(t,{Z:function(){return o}})}}]);