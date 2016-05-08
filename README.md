# nosorog-core

Nosorog Core provides basic functionality like loading and running scripts augmeneted with Nosorog annotations, managing script library etc.

Please note that, as `@Inject` annotations are processed by actual CDI runtime, the code needs to be run inside a CDI container.
Similarly, if Java EE resource injections are used, a Java EE container is required.

JavaScript code:
```javascript
/**
 * import java.util.logging.Logger
 * import java.util.logging.Level
 *
 * import javax.servlet.ServletContext
 * import java.security.Principal
 *
 * @Name("Foo")
 *
 * @Inject ServletContext context
 * @Inject Principal principal
 */

var LOG = Logger.getLogger("foo.js");
LOG.log(Level.INFO, "Hello from script! {0} {1}", [ "foo", "bar" ]);

print(context.contextPath);
print(principal);
```

Java code:
```java
ScriptEngineManager sem = new ScriptEngineManager();
ScriptEngine engine = sem.getEngineByName("nashorn");

ScriptLoader loader = new ScriptLoader();
InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("foo.js");
Script script = loader.load(is);

Object res = script.runWith(engine);
```

Log output:
```
Info:   Hello from script! foo bar
Info:   /nosorog
Info:   ANONYMOUS
```
