file:///D:/GitHub/RESTfulWeb/src/main/java/com/wefky/RESTfulWeb/repository/ImageRepository.java
### java.util.NoSuchElementException: next on empty iterator

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
uri: file:///D:/GitHub/RESTfulWeb/src/main/java/com/wefky/RESTfulWeb/repository/ImageRepository.java
text:
```scala
package com.wefky.RESTfulWeb.repository;

import com.wefky.RESTfulWeb.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query("SELECT i FROM Image i WHERE i.deleted = false")
    List<Image> findAllActive();

    /**
     * Filter active images by optional id, owner (partial), contentType (exact).
     */
    @Query("""
        SELECT i
        FROM Image i
        WHERE i.deleted = false
          AND (:id IS NULL OR i.imageId = :id)
          AND (:owner IS NULL OR LOWER(i.owner) LIKE LOWER(CONCAT('%', :owner, '%')))
          AND (:contentType IS NULL OR LOWER(i.contentType) = LOWER(:contentType))
    """)
    List<Image> filterImages(
            @Param("id") Long id,
            @Param("owner") String owner,
            @Param("contentType") String contentType
    );

    @Query("SELECT i FROM Image i WHERE i.deleted = true")
    List<Image> findAllDeleted();

    /**
     * Filter deleted images (trash).
     */
    @Query("""
        SELECT i
        FROM Image i
        WHERE i.deleted = true
          AND (:id IS NULL OR i.imageId = :id)
          AND (:owner IS NULL OR LOWER(i.owner) LIKE LOWER(CONCAT('%', :owner, '%')))
          AND (:contentType IS NULL OR LOWER(i.contentType) = LOWER(:contentType))
    """)
    List<Image> filterDeletedImages(
            @Param("id") Long id,
            @Param("owner") String owner,
            @Param("contentType") String contentType
    );
}

```



#### Error stacktrace:

```
scala.collection.Iterator$$anon$19.next(Iterator.scala:973)
	scala.collection.Iterator$$anon$19.next(Iterator.scala:971)
	scala.collection.mutable.MutationTracker$CheckedIterator.next(MutationTracker.scala:76)
	scala.collection.IterableOps.head(Iterable.scala:222)
	scala.collection.IterableOps.head$(Iterable.scala:222)
	scala.collection.AbstractIterable.head(Iterable.scala:935)
	dotty.tools.dotc.interactive.InteractiveDriver.run(InteractiveDriver.scala:164)
	dotty.tools.pc.MetalsDriver.run(MetalsDriver.scala:45)
	dotty.tools.pc.WithCompilationUnit.<init>(WithCompilationUnit.scala:31)
	dotty.tools.pc.SimpleCollector.<init>(PcCollector.scala:345)
	dotty.tools.pc.PcSemanticTokensProvider$Collector$.<init>(PcSemanticTokensProvider.scala:63)
	dotty.tools.pc.PcSemanticTokensProvider.Collector$lzyINIT1(PcSemanticTokensProvider.scala:63)
	dotty.tools.pc.PcSemanticTokensProvider.Collector(PcSemanticTokensProvider.scala:63)
	dotty.tools.pc.PcSemanticTokensProvider.provide(PcSemanticTokensProvider.scala:88)
	dotty.tools.pc.ScalaPresentationCompiler.semanticTokens$$anonfun$1(ScalaPresentationCompiler.scala:109)
```
#### Short summary: 

java.util.NoSuchElementException: next on empty iterator