# GraphStream -- Algorithms


[![Build Status](https://travis-ci.org/graphstream/gs-algo.svg?branch=dev)](https://travis-ci.org/graphstream/gs-algo)


The GraphStream project is java library that provides a API to model,
analyze and visualize graphs and dynamic graphs.

Check out the Website <http://www.graphstream-project.org/> for more information.

This package is dedicated to graph and dynamic graph algorithms.

## Installing GraphStream

The release comes with a pre-packaged jar file named gs-algo.jar that contains the GraphStream algorithms classes. It depends on the root project `gs-core`. To start using GraphStream with algorithms, simply put `gs-core.jar` and `gs-algo.jar` in your class path. You can download GraphStream on the github releases pages:

- [gs-core](https://github.com/graphstream/gs-core/releases)
- [gs-algo](https://github.com/graphstream/gs-algo/releases)

Maven users may include major releases of `gs-core` and `gs-algo` as dependencies: 

```xml
<dependencies>
    <dependency>
        <groupId>org.graphstream</groupId>
        <artifactId>gs-core</artifactId>
        <version>2.0</version>
    </dependency>

    <dependency>
        <groupId>org.graphstream</groupId>
        <artifactId>gs-algo</artifactId>
        <version>2.0</version>
    </dependency>
</dependencies>
```

### Development Versions

Using <https://jitpack.io> one can also use any development version. Simply add the `jitpack` repository to the `pom.xml` of the project:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

then, add the `gs-core` and `gs-algo` to your dependencies:

```xml
<dependencies>
    <dependency>
        <groupId>com.github.graphstream</groupId>
        <artifactId>gs-core</artifactId>
        <version>2.0</version>
    </dependency>
    <dependency>
        <groupId>com.github.graphstream</groupId>
        <artifactId>gs-algo</artifactId>
        <version>2.0</version>
    </dependency>
</dependencies>
```


You can use any version of `gs-core` and `gs-algo` you need, provided they are the same. Simply specify the desired version in the `<version>` tag. The version can be a git tag name (e.g. `2.0`), a commit number, or a branch name followed by `-SNAPSHOT` (e.g. `dev-SNAPSHOT`). More details on the [possible versions on jitpack](https://jitpack.io/#graphstream/gs-core).

## Help

You may check the documentation on the website <http://graphstream-project.org>. You may also share your questions on the mailing list at <http://sympa.litislab.fr/sympa/subscribe/graphstream-users>.

## License

See the COPYING file.