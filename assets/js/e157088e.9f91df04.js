(self.webpackChunkjarbird_docs=self.webpackChunkjarbird_docs||[]).push([[406],{3905:function(e,a,n){"use strict";n.d(a,{Zo:function(){return p},kt:function(){return c}});var t=n(7294);function r(e,a,n){return a in e?Object.defineProperty(e,a,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[a]=n,e}function i(e,a){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var t=Object.getOwnPropertySymbols(e);a&&(t=t.filter((function(a){return Object.getOwnPropertyDescriptor(e,a).enumerable}))),n.push.apply(n,t)}return n}function l(e){for(var a=1;a<arguments.length;a++){var n=null!=arguments[a]?arguments[a]:{};a%2?i(Object(n),!0).forEach((function(a){r(e,a,n[a])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):i(Object(n)).forEach((function(a){Object.defineProperty(e,a,Object.getOwnPropertyDescriptor(n,a))}))}return e}function o(e,a){if(null==e)return{};var n,t,r=function(e,a){if(null==e)return{};var n,t,r={},i=Object.keys(e);for(t=0;t<i.length;t++)n=i[t],a.indexOf(n)>=0||(r[n]=e[n]);return r}(e,a);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(t=0;t<i.length;t++)n=i[t],a.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(r[n]=e[n])}return r}var s=t.createContext({}),d=function(e){var a=t.useContext(s),n=a;return e&&(n="function"==typeof e?e(a):l(l({},a),e)),n},p=function(e){var a=d(e.components);return t.createElement(s.Provider,{value:a},e.children)},u={inlineCode:"code",wrapper:function(e){var a=e.children;return t.createElement(t.Fragment,{},a)}},m=t.forwardRef((function(e,a){var n=e.components,r=e.mdxType,i=e.originalType,s=e.parentName,p=o(e,["components","mdxType","originalType","parentName"]),m=d(n),c=r,b=m["".concat(s,".").concat(c)]||m[c]||u[c]||i;return n?t.createElement(b,l(l({ref:a},p),{},{components:n})):t.createElement(b,l({ref:a},p))}));function c(e,a){var n=arguments,r=a&&a.mdxType;if("string"==typeof e||r){var i=n.length,l=new Array(i);l[0]=m;var o={};for(var s in a)hasOwnProperty.call(a,s)&&(o[s]=a[s]);o.originalType=e,o.mdxType="string"==typeof e?e:r,l[1]=o;for(var d=2;d<i;d++)l[d]=n[d];return t.createElement.apply(null,l)}return t.createElement.apply(null,n)}m.displayName="MDXCreateElement"},8215:function(e,a,n){"use strict";var t=n(7294);a.Z=function(e){var a=e.children,n=e.hidden,r=e.className;return t.createElement("div",{role:"tabpanel",hidden:n,className:r},a)}},1395:function(e,a,n){"use strict";n.d(a,{Z:function(){return p}});var t=n(7294),r=n(944),i=n(6010),l="tabItem_1uMI",o="tabItemActive_2DSg";var s=37,d=39;var p=function(e){var a=e.lazy,n=e.block,p=e.defaultValue,u=e.values,m=e.groupId,c=e.className,b=(0,r.Z)(),h=b.tabGroupChoices,f=b.setTabGroupChoices,g=(0,t.useState)(p),k=g[0],v=g[1],y=t.Children.toArray(e.children),N=[];if(null!=m){var w=h[m];null!=w&&w!==k&&u.some((function(e){return e.value===w}))&&v(w)}var C=function(e){var a=e.currentTarget,n=N.indexOf(a),t=u[n].value;v(t),null!=m&&(f(m,t),setTimeout((function(){var e,n,t,r,i,l,s,d;(e=a.getBoundingClientRect(),n=e.top,t=e.left,r=e.bottom,i=e.right,l=window,s=l.innerHeight,d=l.innerWidth,n>=0&&i<=d&&r<=s&&t>=0)||(a.scrollIntoView({block:"center",behavior:"smooth"}),a.classList.add(o),setTimeout((function(){return a.classList.remove(o)}),2e3))}),150))},j=function(e){var a,n;switch(e.keyCode){case d:var t=N.indexOf(e.target)+1;n=N[t]||N[0];break;case s:var r=N.indexOf(e.target)-1;n=N[r]||N[N.length-1]}null==(a=n)||a.focus()};return t.createElement("div",{className:"tabs-container"},t.createElement("ul",{role:"tablist","aria-orientation":"horizontal",className:(0,i.Z)("tabs",{"tabs--block":n},c)},u.map((function(e){var a=e.value,n=e.label;return t.createElement("li",{role:"tab",tabIndex:k===a?0:-1,"aria-selected":k===a,className:(0,i.Z)("tabs__item",l,{"tabs__item--active":k===a}),key:a,ref:function(e){return N.push(e)},onKeyDown:j,onFocus:C,onClick:C},n)}))),a?(0,t.cloneElement)(y.filter((function(e){return e.props.value===k}))[0],{className:"margin-vert--md"}):t.createElement("div",{className:"margin-vert--md"},y.map((function(e,a){return(0,t.cloneElement)(e,{key:a,hidden:e.props.value!==k})}))))}},9443:function(e,a,n){"use strict";var t=(0,n(7294).createContext)(void 0);a.Z=t},944:function(e,a,n){"use strict";var t=n(7294),r=n(9443);a.Z=function(){var e=(0,t.useContext)(r.Z);if(null==e)throw new Error("`useUserPreferencesContext` is used outside of `Layout` Component.");return e}},1281:function(e,a,n){"use strict";n.r(a),n.d(a,{frontMatter:function(){return s},metadata:function(){return d},toc:function(){return p},default:function(){return m}});var t=n(2122),r=n(9756),i=(n(7294),n(3905)),l=n(1395),o=n(8215),s={title:"Publishing Android Components",tags:"tutorial",sidebar_label:"Android Components",sidebar_position:6},d={unversionedId:"tutorials/android",id:"tutorials/android",isDocsHomePage:!1,title:"Publishing Android Components",description:"In this tutorial, we are going to publish Android AAR library to Maven Central.",source:"@site/docs/tutorials/android.mdx",sourceDirName:"tutorials",slug:"/tutorials/android",permalink:"/jarbird/docs/tutorials/android",editUrl:"https://github.com/hkhc/jarbird-docs/docs/tutorials/android.mdx",version:"current",sidebar_label:"Android Components",sidebarPosition:6,frontMatter:{title:"Publishing Android Components",tags:"tutorial",sidebar_label:"Android Components",sidebar_position:6},sidebar:"tutorialSidebar",previous:{title:"Publish to custom Maven repositories",permalink:"/jarbird/docs/tutorials/custommaven"},next:{title:"Publish to custom Artifactory repositories",permalink:"/jarbird/docs/tutorials/artifactory"}},p=[{value:"pom.yaml",id:"pomyaml",children:[]},{value:"build.gradle",id:"buildgradle",children:[]},{value:"Run It",id:"run-it",children:[]},{value:"Variant with artifactId",id:"variant-with-artifactid",children:[]},{value:"Variant with POM",id:"variant-with-pom",children:[]}],u={toc:p};function m(e){var a=e.components,n=(0,r.Z)(e,["components"]);return(0,i.kt)("wrapper",(0,t.Z)({},u,n,{components:a,mdxType:"MDXLayout"}),(0,i.kt)("p",null,"In this tutorial, we are going to publish Android AAR library to Maven Central."),(0,i.kt)("p",null,"The sample project is ","[here]","(",(0,i.kt)("a",{parentName:"p",href:"https://github."},"https://github."),"\ncom/hkhc/jarbird-samples/tree/master/android)."),(0,i.kt)("p",null,"Unlike publishing  conventional JAR libraries, we need to take care of the\n",(0,i.kt)("strong",{parentName:"p"},"Build Types"),", ",(0,i.kt)("strong",{parentName:"p"},"Flavors"),", and ",(0,i.kt)("strong",{parentName:"p"},"Variants")," of Android library projects."),(0,i.kt)("p",null,"A variant is a combination of a build type and a flavor, so that it may refer\nto a combination of source set, resources, etc. For example, a typical Android project\nmay have build types ",(0,i.kt)("inlineCode",{parentName:"p"},"debug")," and ",(0,i.kt)("inlineCode",{parentName:"p"},"release"),", and flavors for ",(0,i.kt)("inlineCode",{parentName:"p"},"qa")," and\n",(0,i.kt)("inlineCode",{parentName:"p"},"production"),". Then we have four different variants:"),(0,i.kt)("ul",null,(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("inlineCode",{parentName:"li"},"qaDebug")),(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("inlineCode",{parentName:"li"},"qaRelease")),(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("inlineCode",{parentName:"li"},"productionDebug")),(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("inlineCode",{parentName:"li"},"productionRelease"))),(0,i.kt)("p",null,"(We could ignore some combinations that do not make sense for particular\nproject, please refer to ",(0,i.kt)("inlineCode",{parentName:"p"},"variantFilter")," in ",(0,i.kt)("a",{parentName:"p",href:"https://developer.android.com/studio/build/build-variants"},"Android documentation")," for details)"),(0,i.kt)("p",null,"Each of the variants creates a different library and can be published\n(or not to be published) individually. Jarbird plugin supports configure\nhow each of these components is published."),(0,i.kt)("p",null,"The source code of this tutorial is in the ",(0,i.kt)("inlineCode",{parentName:"p"},"android")," directory."),(0,i.kt)("h3",{id:"pomyaml"},"pom.yaml"),(0,i.kt)("p",null,"The ",(0,i.kt)("inlineCode",{parentName:"p"},"pom.yaml")," file is not that different from conventional JAR publishing.\nThe only difference right now is the ",(0,i.kt)("inlineCode",{parentName:"p"},"packaging")," line."),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-yaml",metastring:'title="pom.yaml" {4}',title:'"pom.yaml"',"{4}":!0},"group: jarbirdsamples\nartifactId: simpleaar\nversion: 1.0\npackaging: aar\n\nlicenses:\n  - name: Apache-2.0\n    dist: repo\n\ndevelopers:\n  - id: demo\n    name: Jarbird Demo\n    email: jarbird.demo@fake-email.com\n\nscm:\n  repoType: github.com\n  repoName: demo/jarbird-samples/android\n")),(0,i.kt)("h3",{id:"buildgradle"},"build.gradle"),(0,i.kt)("p",null,"We have a different plugin ID for Android project:"),(0,i.kt)(l.Z,{defaultValue:"buildgradle-1",values:[{label:"build.gradle",value:"buildgradle-1"},{label:"build.gradle.kts",value:"buildgradlekts"}],mdxType:"Tabs"},(0,i.kt)(o.Z,{value:"buildgradle-1",mdxType:"TabItem"},(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-groovy",metastring:"{4}","{4}":!0},"plugins {\n    id 'com.android.library'\n    id 'kotlin-android'\n    id 'io.hkhc.jarbird-android' version \"0.6.0\"\n}\n"))),(0,i.kt)(o.Z,{value:"buildgradlekts",mdxType:"TabItem"},(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-kotlin",metastring:"{4}","{4}":!0},'plugins {\n    id("com.android.library")\n    id("kotlin-android")\n    id("io.hkhc.jarbird-android") version "0.6.0"\n}\n')))),(0,i.kt)("p",null,"There is no new syntax and functions in the plugin ",(0,i.kt)("inlineCode",{parentName:"p"},"io.hkhc.jarbird-android"),".\nIt just has additional capability to recognize Android ",(0,i.kt)("inlineCode",{parentName:"p"},"LibraryVariant")," objects.\nIt does not hurt to use this plugin on conventional JAR projects. Just looks confusing."),(0,i.kt)("p",null,"The build script of typical Android library projects has an android block\nto configure the build types and flavors. We add our code after the\n",(0,i.kt)("inlineCode",{parentName:"p"},"android")," block to specify how they are published, in the Jarbird way."),(0,i.kt)(l.Z,{defaultValue:"buildgradle-2",values:[{label:"build.gradle",value:"buildgradle-2"},{label:"build.gradle.kts",value:"buildgradlekts-1"}],mdxType:"Tabs"},(0,i.kt)(o.Z,{value:"buildgradle-2",mdxType:"TabItem"},(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-groovy",metastring:"{4,8}","{4,8}":!0},"// after android {}\nandroid.libraryVariants.configureEach { variant ->\n    jarbird {\n        pub(variant.name) {\n            mavenCentral()\n            from(variant)\n        }\n    }\n}\n"))),(0,i.kt)(o.Z,{value:"buildgradlekts-1",mdxType:"TabItem"},(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-kotlin",metastring:"{4,8}","{4,8}":!0},"// after android {}\nandroid.libraryVariants.configureEach {\n    jarbird {\n        pub(name) {\n            mavenCentral()\n            from(this@configureEach)\n        }\n    }\n}\n")))),(0,i.kt)("p",null,"Note how Kotlin and Groovy differ in getting the proper reference to the ",(0,i.kt)("inlineCode",{parentName:"p"},"from()"),"\nmethod. Because of the ",(0,i.kt)("inlineCode",{parentName:"p"},"configureEach")," loop, we created two ",(0,i.kt)("inlineCode",{parentName:"p"},"pub"),"s, one for\neach variant, and the results are two sets of artefacts."),(0,i.kt)("p",null,'There are two things in the "jarbird" block that differs from our previous tutorials.\nFirst the additional ',(0,i.kt)("inlineCode",{parentName:"p"},"from()")," block that we have mentioned. When building\nconventional JAR libraries, we normally don't need to specify where the source\ncode comes from. The plugin will figure it out by itself. When publishing Android\nAAR components, we need to tell what to publish by passing the ",(0,i.kt)("inlineCode",{parentName:"p"},"LibraryVariant"),"\nreference to ",(0,i.kt)("inlineCode",{parentName:"p"},"from()")," method."),(0,i.kt)("p",null,"The other thing to notice is that we provided a name to ",(0,i.kt)("inlineCode",{parentName:"p"},"pub"),".\nIt is used to identify them when there are multiple ",(0,i.kt)("inlineCode",{parentName:"p"},"pub"),".\nIt could be any string as long as it is unique among ",(0,i.kt)("inlineCode",{parentName:"p"},"pub"),"s. Conventionally we\nuse the variant name of the Android project."),(0,i.kt)("p",null,"Normally Android library has at least two build types, ",(0,i.kt)("inlineCode",{parentName:"p"},"debug")," and ",(0,i.kt)("inlineCode",{parentName:"p"},"release"),"."),(0,i.kt)("h2",{id:"run-it"},"Run It"),(0,i.kt)("p",null,"Executing ",(0,i.kt)("inlineCode",{parentName:"p"},"./gradlew jbPublishToMavenLocal")," we get two publications:"),(0,i.kt)("ul",null,(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("inlineCode",{parentName:"li"},"~/.m2/repository/jarbirdsamples/simpleaar/1.0-release")),(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("inlineCode",{parentName:"li"},"~/.m2/repository/jarbirdsamples/simpleaar/1.0-debug"))),(0,i.kt)("p",null,"The artifacts will look like:"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-shell-session"},"ls -1 ~/.m2/repository/jarbirdsamples/simpleaar/1.0-release\nsimpleaar-1.0-release-javadoc.jar\nsimpleaar-1.0-release-javadoc.jar.asc\nsimpleaar-1.0-release-sources.jar\nsimpleaar-1.0-release-sources.jar.asc\nsimpleaar-1.0-release.aar\nsimpleaar-1.0-release.aar.asc\nsimpleaar-1.0-release.module\nsimpleaar-1.0-release.module.asc\nsimpleaar-1.0-release.pom\nsimpleaar-1.0-release.pom.asc\n")),(0,i.kt)("p",null,"This is the default way to distinguish different publications, that the version\nis suffixed by the variant. However, we can change that."),(0,i.kt)("h2",{id:"variant-with-artifactid"},"Variant with artifactId"),(0,i.kt)("p",null,"We may make the variant part of the artifactId. We add one line to the Jarbird configuration:"),(0,i.kt)(l.Z,{defaultValue:"buildgradle-3",values:[{label:"build.gradle",value:"buildgradle-3"},{label:"build.gradle.kts",value:"buildgradlekts-2"}],mdxType:"Tabs"},(0,i.kt)(o.Z,{value:"buildgradle-3",mdxType:"TabItem"},(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-groovy",metastring:"{6}","{6}":!0},"android.libraryVariants.configureEach { variant ->\n    jarbird {\n        pub(variant.name) {\n            mavenCentral()\n            variantWithArtifactId()\n            from(variant)\n        }\n    }\n}\n"))),(0,i.kt)(o.Z,{value:"buildgradlekts-2",mdxType:"TabItem"},(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-kotlin",metastring:"{6}","{6}":!0},"android.libraryVariants.configureEach {\n    jarbird {\n        pub(name) {\n            mavenCentral()\n            variantWithArtifactId()\n            from(this@configureEach)\n        }\n    }\n}\n")))),(0,i.kt)("p",null,"The additional line indicates the artifactId shall be suffixed by the variant.\nAfter executing ",(0,i.kt)("inlineCode",{parentName:"p"},"jbPublishToMavenLocal"),", we get two publications with\ndirectories like this:"),(0,i.kt)("ul",null,(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("inlineCode",{parentName:"li"},"~/.m2/repository/jarbirdsamples/simpleaar-release/1.0")),(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("inlineCode",{parentName:"li"},"~/.m2/repository/jarbirdsamples/simpleaar-debug/1.0"))),(0,i.kt)("p",null,"We could have even greater control to the coordinate of components by specifying variant in ",(0,i.kt)("inlineCode",{parentName:"p"},"pom.yaml")),(0,i.kt)("h2",{id:"variant-with-pom"},"Variant with POM"),(0,i.kt)("p",null,"We may customize the POM information for each variant in ",(0,i.kt)("inlineCode",{parentName:"p"},"pom.xml"),". There\nare two ways to do so. In this tutorial, we will focus on the one method first."),(0,i.kt)("p",null,"Let's change the ",(0,i.kt)("inlineCode",{parentName:"p"},"pom.yaml")," like this:"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-yaml",metastring:'title="pom.yaml" {1-6}',title:'"pom.yaml"',"{1-6}":!0},"variant: debug\nartifactId: simpleaar-staging\n---\nvariant: release\nartifactId: simpleaar-production\n---\ngroup: jarbirdsamples\nversion: 1.0\npackaging: aar\n\nlicenses:\n  - name: Apache-2.0\n    dist: repo\n\ndevelopers:\n  - id: demo\n    name: Jarbird Demo\n    email: jarbird.demo@fake-email.com\n\nscm:\n  repoType: github.com\n  repoName: demo/jarbird-samples/android\n")),(0,i.kt)("p",null,"Now our ",(0,i.kt)("inlineCode",{parentName:"p"},"pom.yaml")," has three sections, two of them have a ",(0,i.kt)("inlineCode",{parentName:"p"},"variant")," attribute.\nWhen publishing components, Jarbird plugin ",(0,i.kt)("strong",{parentName:"p"},"combine the section in ",(0,i.kt)("inlineCode",{parentName:"strong"},"pom.yaml"),"\nwith ",(0,i.kt)("inlineCode",{parentName:"strong"},"variant")," attribute and the section without ",(0,i.kt)("inlineCode",{parentName:"strong"},"variant"),".")," Therefore\neffectively we have different ",(0,i.kt)("inlineCode",{parentName:"p"},"pom.yaml")," for each of the variants."),(0,i.kt)("p",null,"Then we add a line to the ",(0,i.kt)("inlineCode",{parentName:"p"},"build.gradle")," file."),(0,i.kt)(l.Z,{defaultValue:"buildgradle-4",values:[{label:"build.gradle",value:"buildgradle-4"},{label:"build.gradle.kts",value:"buildgradlekts-3"}],mdxType:"Tabs"},(0,i.kt)(o.Z,{value:"buildgradle-4",mdxType:"TabItem"},(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-groovy",metastring:"{6}","{6}":!0},"android.libraryVariants.configureEach { variant ->\n    jarbird {\n        pub(variant.name) {\n            mavenCentral()\n            variantInvisible()\n            from(variant)\n        }\n    }\n}\n"))),(0,i.kt)(o.Z,{value:"buildgradlekts-3",mdxType:"TabItem"},(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-kotlin",metastring:"{6}","{6}":!0},"android.libraryVariants.configureEach {\n    jarbird {\n        pub(name) {\n            mavenCentral()\n            variantInvisible()\n            from(this@configureEach)\n        }\n    }\n}\n")))),(0,i.kt)("p",null,"The line ",(0,i.kt)("inlineCode",{parentName:"p"},"variantInvisible()")," tell Jarbird plugin not to merge the variant name.\nSo the information of components we built is entirely on ",(0,i.kt)("inlineCode",{parentName:"p"},"pom.yaml"),"."),(0,i.kt)("p",null,"Executing ",(0,i.kt)("inlineCode",{parentName:"p"},"./gradlew jbPublishToMavenLocal")," and we will get the following in\nMaven Local repository:"),(0,i.kt)("ul",null,(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("inlineCode",{parentName:"li"},"~/.m2/repository/jarbirdsamples/simpleaar-production/1.0")),(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("inlineCode",{parentName:"li"},"~/.m2/repository/jarbirdsamples/simpleaar-staging/1.0"))),(0,i.kt)("p",null,"The artifacts of the release variant look like this:"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-shell-session"},"$ ls -1 ~/.m2/repository/jarbirdsamples/simpleaar-production/1.0\nsimpleaar-production-1.0-javadoc.jar\nsimpleaar-production-1.0-javadoc.jar.asc\nsimpleaar-production-1.0-sources.jar\nsimpleaar-production-1.0-sources.jar.asc\nsimpleaar-production-1.0.aar\nsimpleaar-production-1.0.aar.asc\nsimpleaar-production-1.0.module\nsimpleaar-production-1.0.module.asc\nsimpleaar-production-1.0.pom\nsimpleaar-production-1.0.pom.asc\n")))}m.isMDXComponent=!0},6010:function(e,a,n){"use strict";function t(e){var a,n,r="";if("string"==typeof e||"number"==typeof e)r+=e;else if("object"==typeof e)if(Array.isArray(e))for(a=0;a<e.length;a++)e[a]&&(n=t(e[a]))&&(r&&(r+=" "),r+=n);else for(a in e)e[a]&&(r&&(r+=" "),r+=a);return r}function r(){for(var e,a,n=0,r="";n<arguments.length;)(e=arguments[n++])&&(a=t(e))&&(r&&(r+=" "),r+=a);return r}n.d(a,{Z:function(){return r}})}}]);