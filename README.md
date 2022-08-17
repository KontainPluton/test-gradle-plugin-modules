### GRADLE MODULE PLUGIN

Ce repo est basé sur un ancien plugin gradle existant. Il permet de simplifier l'utilisation des modules dans gradle (quand gradle n'était pas encore très au point avec ceux-ci). Le plugin par défaut ne fait pas ce que l'on veut, mais j'ai repris son code car il pouvait être utile au vu du fonctionnement du plugin (il permet de reconnaitre les modules java et des la ajouter au classpath / modulepath pour que java puisse compiler). 

Il n'y a pas tout les tests que j'ai pu faire car j'en ai supprimé certains, mais voila le repo tel que je l'ai laissé. C'est un repo dense, mais les parties interessantes sont surtout dans `src/main/java/org/javamodularity/moduleplugin/tasks/` ou on retrouve par exemple `CompileTask`.

--------------------

#### Forum gradle :

Premier post que j'ai fait :
https://discuss.gradle.org/t/java-modules-jpms-compile-all-modules-at-once/43548
Je n'ai pas réussi à faire marcher la technique proposée.
Cette première solution serait de faire une alternative à la commande :
`javac -d mods --module-source-path "./*/src/main/java/" $(find -name "*.java")
` en modifiant la tache JavaCompile en ajoutant des arguments.
```
tasks.withType<JavaCompile> {
    val compilerArgs = options.compilerArgs
    compilerArgs.add("--module-path,classpath.asPath")
    classpath = files()
}
```

```
tasks.compileJava {
    options.compilerArgs.add("-d")
    options.compilerArgs.add("mods")
    options.compilerArgs.add("--module-source-path")
    options.compilerArgs.add("\"../*/src/main/java/\"")
[...]
```


Je suis donc parti sur un plugin permettant de faire ce que l'on veut.
https://discuss.gradle.org/t/java-modules-jpms-compile-all-modules-gradle-plugin/56345
Sur ce dernier post personne ne m'a répondu.

--------------------

#### Doc :

- User manual gradle : https://docs.gradle.org/current/userguide/userguide.html
- Javadoc gradle : https://docs.gradle.org/current/javadoc/
- Doc gradle sur les plugins custom : https://docs.gradle.org/current/userguide/custom_plugins.html
- Blog d'un ancien de chez gradle : https://melix.github.io/blog/tags/gradle.html
- Github d'un développeur de chez gradle : https://github.com/jjohannes?tab=repositories (Il a mis en ligne plusieurs plugin pour gérer plus facilement les modules java, comme : https://github.com/jjohannes/java-module-dependencies permettant de gérer les dépendances entre les modules, https://github.com/jjohannes/java-module-testing facilitant les tests sur les modules ou encore https://github.com/jjohannes/extra-java-module-info permettant d'avori des informations supplémentaires sur les modules)