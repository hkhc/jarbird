(self.webpackChunkjarbird_docs=self.webpackChunkjarbird_docs||[]).push([[297],{3905:function(e,t,n){"use strict";n.d(t,{Zo:function(){return p},kt:function(){return m}});var r=n(7294);function a(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function i(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function l(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?i(Object(n),!0).forEach((function(t){a(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):i(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function o(e,t){if(null==e)return{};var n,r,a=function(e,t){if(null==e)return{};var n,r,a={},i=Object.keys(e);for(r=0;r<i.length;r++)n=i[r],t.indexOf(n)>=0||(a[n]=e[n]);return a}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(r=0;r<i.length;r++)n=i[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(a[n]=e[n])}return a}var u=r.createContext({}),s=function(e){var t=r.useContext(u),n=t;return e&&(n="function"==typeof e?e(t):l(l({},t),e)),n},p=function(e){var t=s(e.components);return r.createElement(u.Provider,{value:t},e.children)},d={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},c=r.forwardRef((function(e,t){var n=e.components,a=e.mdxType,i=e.originalType,u=e.parentName,p=o(e,["components","mdxType","originalType","parentName"]),c=s(n),m=a,g=c["".concat(u,".").concat(m)]||c[m]||d[m]||i;return n?r.createElement(g,l(l({ref:t},p),{},{components:n})):r.createElement(g,l({ref:t},p))}));function m(e,t){var n=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var i=n.length,l=new Array(i);l[0]=c;var o={};for(var u in t)hasOwnProperty.call(t,u)&&(o[u]=t[u]);o.originalType=e,o.mdxType="string"==typeof e?e:a,l[1]=o;for(var s=2;s<i;s++)l[s]=n[s];return r.createElement.apply(null,l)}return r.createElement.apply(null,n)}c.displayName="MDXCreateElement"},8215:function(e,t,n){"use strict";var r=n(7294);t.Z=function(e){var t=e.children,n=e.hidden,a=e.className;return r.createElement("div",{role:"tabpanel",hidden:n,className:a},t)}},1395:function(e,t,n){"use strict";n.d(t,{Z:function(){return p}});var r=n(7294),a=n(944),i=n(6010),l="tabItem_1uMI",o="tabItemActive_2DSg";var u=37,s=39;var p=function(e){var t=e.lazy,n=e.block,p=e.defaultValue,d=e.values,c=e.groupId,m=e.className,g=(0,a.Z)(),b=g.tabGroupChoices,f=g.setTabGroupChoices,h=(0,r.useState)(p),y=h[0],v=h[1],k=r.Children.toArray(e.children),P=[];if(null!=c){var w=b[c];null!=w&&w!==y&&d.some((function(e){return e.value===w}))&&v(w)}var j=function(e){var t=e.currentTarget,n=P.indexOf(t),r=d[n].value;v(r),null!=c&&(f(c,r),setTimeout((function(){var e,n,r,a,i,l,u,s;(e=t.getBoundingClientRect(),n=e.top,r=e.left,a=e.bottom,i=e.right,l=window,u=l.innerHeight,s=l.innerWidth,n>=0&&i<=s&&a<=u&&r>=0)||(t.scrollIntoView({block:"center",behavior:"smooth"}),t.classList.add(o),setTimeout((function(){return t.classList.remove(o)}),2e3))}),150))},N=function(e){var t,n;switch(e.keyCode){case s:var r=P.indexOf(e.target)+1;n=P[r]||P[0];break;case u:var a=P.indexOf(e.target)-1;n=P[a]||P[P.length-1]}null==(t=n)||t.focus()};return r.createElement("div",{className:"tabs-container"},r.createElement("ul",{role:"tablist","aria-orientation":"horizontal",className:(0,i.Z)("tabs",{"tabs--block":n},m)},d.map((function(e){var t=e.value,n=e.label;return r.createElement("li",{role:"tab",tabIndex:y===t?0:-1,"aria-selected":y===t,className:(0,i.Z)("tabs__item",l,{"tabs__item--active":y===t}),key:t,ref:function(e){return P.push(e)},onKeyDown:N,onFocus:j,onClick:j},n)}))),t?(0,r.cloneElement)(k.filter((function(e){return e.props.value===y}))[0],{className:"margin-vert--md"}):r.createElement("div",{className:"margin-vert--md"},k.map((function(e,t){return(0,r.cloneElement)(e,{key:t,hidden:e.props.value!==y})}))))}},9443:function(e,t,n){"use strict";var r=(0,n(7294).createContext)(void 0);t.Z=r},944:function(e,t,n){"use strict";var r=n(7294),a=n(9443);t.Z=function(){var e=(0,r.useContext)(a.Z);if(null==e)throw new Error("`useUserPreferencesContext` is used outside of `Layout` Component.");return e}},3191:function(e,t,n){"use strict";n.r(t),n.d(t,{frontMatter:function(){return u},metadata:function(){return s},toc:function(){return p},default:function(){return c}});var r=n(2122),a=n(9756),i=(n(7294),n(3905)),l=n(1395),o=n(8215),u={title:"Publishing Gradle Plugin",tags:"tutorial",sidebar_label:"Gradle Plugin",sidebar_position:6},s={unversionedId:"tutorials/gradleplugin",id:"tutorials/gradleplugin",isDocsHomePage:!1,title:"Publishing Gradle Plugin",description:"We have build different type of libraries with Gradle. This time we are going",source:"@site/docs/tutorials/gradleplugin.mdx",sourceDirName:"tutorials",slug:"/tutorials/gradleplugin",permalink:"/jarbird/docs/tutorials/gradleplugin",editUrl:"https://github.com/hkhc/jarbird-docs/docs/tutorials/gradleplugin.mdx",version:"current",sidebar_label:"Gradle Plugin",sidebarPosition:6,frontMatter:{title:"Publishing Gradle Plugin",tags:"tutorial",sidebar_label:"Gradle Plugin",sidebar_position:6},sidebar:"tutorialSidebar",previous:{title:"Publishing Android Components",permalink:"/jarbird/docs/tutorials/android"}},p=[{value:"Gradle Portal account",id:"gradle-portal-account",children:[]},{value:"pom.yaml",id:"pomyaml",children:[]},{value:"build.gradle",id:"buildgradle",children:[]}],d={toc:p};function c(e){var t=e.components,n=(0,a.Z)(e,["components"]);return(0,i.kt)("wrapper",(0,r.Z)({},d,n,{components:t,mdxType:"MDXLayout"}),(0,i.kt)("p",null,"We have build different type of libraries with Gradle. This time we are going\nto build Gradle Plugin wirth Jarbird."),(0,i.kt)("p",null,"The sample project is ","[here]","(",(0,i.kt)("a",{parentName:"p",href:"https://github."},"https://github."),"\ncom/hkhc/jarbird-samples/tree/master/gradleplugin)"),(0,i.kt)("p",null,"From build script perspective, building Gradle Plugin basically the same as building\nconventional JAR libraries. All additional things are in the ",(0,i.kt)("inlineCode",{parentName:"p"},"pom.yaml")),(0,i.kt)("p",null,"Please refer to the ",(0,i.kt)("inlineCode",{parentName:"p"},"gradleplugin")," project in the ",(0,i.kt)("inlineCode",{parentName:"p"},"jarbirdsamples")," GitHub repo."),(0,i.kt)("p",null,"Note that if we publish Gradle Plugin in old way, that no plugin ID is defined,\nwe don't need any special means to publish it. It is just a conventional JAR\nlibrary. In this section we focus on publishing new style Gradle Plugin that\nhave plugin ID."),(0,i.kt)("h2",{id:"gradle-portal-account"},"Gradle Portal account"),(0,i.kt)("p",null,"An account is needed to publish to ",(0,i.kt)("a",{parentName:"p",href:"https://plugins.gradle.org/"},"Gradle Plugin Portal"),"."),(0,i.kt)("h2",{id:"pomyaml"},"pom.yaml"),(0,i.kt)("p",null,"Beside the usual information, we added one more section for Gradle plugin:"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-yaml",metastring:'title="pom.yaml" {6-9}',title:'"pom.yaml"',"{6-9}":!0},"group: jarbirdsamples\nartifactId: gradleplugin\nversion: 1.0\npackaging: jar\n# ...the rest of pom.yml\nplugin:\n  id: jarbirdsamples.plugin\n  displayName: Demo of simple Gradle Plugin\n  implementationClass: simplelib.SimplePlugin\n")),(0,i.kt)("p",null,"From remote repository perspective, a Gradle plugin consists of two\nparts. "),(0,i.kt)("p",null,"One is the JAR library that contains the Gradle plugin code\n(",(0,i.kt)("inlineCode",{parentName:"p"},"jarbirdsamples:gradleplugin:1.0"),' in this example).\nIt is published just like any other JAR libraries. The other part is the\nso-called "Plugin Marker Publication", that relate the plugin ID used in the\nnew style ',(0,i.kt)("inlineCode",{parentName:"p"},"plugins")," block in build script, to the JAR library of the plugin itself."),(0,i.kt)("p",null,"So define the plugin ID as above (",(0,i.kt)("inlineCode",{parentName:"p"},"jarbirdsamples.plug"),"), and the implementation\nclass of the plugin (",(0,i.kt)("inlineCode",{parentName:"p"},"simplelib.SimplePlugin"),"). Then we can build the plugin\nas usual with ",(0,i.kt)("inlineCode",{parentName:"p"},"./gradlew jbPublishToMavenLocal"),"."),(0,i.kt)("h2",{id:"buildgradle"},"build.gradle"),(0,i.kt)("p",null,"To make it easier to use Gradle Plugin, we usually publish it to Gradle Plugin\nPortal. It is essentially a Maven repository that Gradle use it first to find\nplugin. So we don't need to configure repository in ",(0,i.kt)("inlineCode",{parentName:"p"},"pluginManagement")," of\n",(0,i.kt)("inlineCode",{parentName:"p"},"settings.gradle")," to make the plugin accessible."),(0,i.kt)(l.Z,{defaultValue:"groovy",values:[{label:"Groovy",value:"groovy"},{label:"Kotlin",value:"kotlin"}],mdxType:"Tabs"},(0,i.kt)(o.Z,{value:"groovy",mdxType:"TabItem"},(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-groovy",metastring:'title="build.gradle" {5}',title:'"build.gradle"',"{5}":!0},"// ...\njarbird {\n    pub {\n        mavenLocal()\n        gradlePortal()\n        useGpg = true\n    }\n}\n// ...\n"))),(0,i.kt)(o.Z,{value:"kotlin",mdxType:"TabItem"},(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-kotlin",metastring:'title="build.gradle.kts" {5}',title:'"build.gradle.kts"',"{5}":!0},"// ...\njarbird {\n    pub {\n        mavenLocal()\n        gradlePortal()\n        useGpg = true\n    }\n}\n// ...\n")))),(0,i.kt)("p",null,"So we can publish the Gradle Plugin to Maven Local or Gradle Portal by\n",(0,i.kt)("inlineCode",{parentName:"p"},"jbPublishToMavenLocal")," or ",(0,i.kt)("inlineCode",{parentName:"p"},"jbPublishToGradlePortal")," respectively. "),(0,i.kt)("p",null,"We can also publish Gradle Plugin to Maven Central by specifying\n",(0,i.kt)("inlineCode",{parentName:"p"},"mavenCentral()")," in ",(0,i.kt)("inlineCode",{parentName:"p"},"pub")," block."),(0,i.kt)("p",null,"If it is published to Maven Local repository, we may inspect the result. Two\ncomponents are published, and they are at:"),(0,i.kt)("ul",null,(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("inlineCode",{parentName:"li"},"~/.m2/repository/jarbirdsamples/gradleplugin/1.0")),(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("inlineCode",{parentName:"li"},"~/.m2/repository/jarbirdsamples/plugin/jarbirdsamples.plugin.gradle.\nplugin/1.0"))),(0,i.kt)("p",null,"The latter is so-called plugin marker publication that is used by Gradle to\nidentify plugin by plugin ID. ",(0,i.kt)("inlineCode",{parentName:"p"},"jarbirdsamples.plugin")," is the plugin ID."))}c.isMDXComponent=!0},6010:function(e,t,n){"use strict";function r(e){var t,n,a="";if("string"==typeof e||"number"==typeof e)a+=e;else if("object"==typeof e)if(Array.isArray(e))for(t=0;t<e.length;t++)e[t]&&(n=r(e[t]))&&(a&&(a+=" "),a+=n);else for(t in e)e[t]&&(a&&(a+=" "),a+=t);return a}function a(){for(var e,t,n=0,a="";n<arguments.length;)(e=arguments[n++])&&(t=r(e))&&(a&&(a+=" "),a+=t);return a}n.d(t,{Z:function(){return a}})}}]);