# Why OneDev overrides Hibernate

OneDev overrides eight Hibernate classes in `server-core/src/main/java/org/hibernate`.
The reasons below were checked against Git history and the source jars for the
versions in `pom.xml`: Hibernate ORM 7.4.8.Final and Hibernate Validator 9.1.3.Final.
The [library override audit](library-customizations.md) also covers the Jakarta
web stack and the removal of the now-redundant Commons Lang proxy-cloning patch.

| Customized class | Why OneDev needs it | Regression coverage |
| --- | --- | --- |
| `cache.spi.support.SimpleTimestamper` | Replace the clock-based busy loop with an atomic maximum of wall-clock time and the previous counter plus one to prevent excessive CPU usage during cache timestamp generation. Introduced in `8de9b24808` for #711. | `SimpleTimestamperTest`: a counter ahead of the wall clock still advances; concurrent cache users get unique, increasing timestamps. The clock regression uses an isolated counter, not the machine clock. |
| `proxy.pojo.BasicLazyInitializer` | Apply `AbstractEntity` ID equality and hashing without loading the entity. Added with the large-user-count performance improvements in `a32c76862a` (#875). Collection membership checks should not fetch every referenced user. | `BasicLazyInitializerTest`: detached references with equal/different IDs, loaded entities, null/unrelated values, unsaved identity semantics, and ordinary method delegation. Calls the interceptor directly because `AbstractEntity`'s final methods can bypass interception on real proxies. |
| `validator.internal.metadata.aggregated.MetaDataBuilder` | Let editable subclasses replace constraint attributes instead of combining parent and child annotations of the same type. Introduced in `de278c1554`. | `HibernateValidationInheritanceTest`: metadata contains the child's `@Size`, and value validation uses its limit and message. |
| `validator.internal.engine.MethodValidationConfiguration` | Permit `@Valid` on both overridden and overriding getters, consistent with OneDev's getter replacement semantics. Added in `e3b23bd5ba` for Kaniko build-spec validation, OD-2564. | `HibernateValidationInheritanceTest`: redeclared object and collection cascades build metadata successfully and still report invalid children. |
| `validator.internal.engine.ValidatorImpl` | Respect removed/overridden getter constraints and cascades; skip fields and nested beans hidden by `@DependsOn` or `@ShowCondition`; validate properties and nested beans before `@ClassValidating`, and suppress class validation after failures. The changes span `de278c1554`, `8ac7615738`, `77cf1da58c`, and `e3b23bd5ba`. Display-name lookup fixes hidden parameters (OD-2560, `0defe8b62a`); repeated dependencies support multiple visibility conditions (OD-2884, `91457af05e`). | `HibernateValidationInheritanceTest`, `HibernateValidationVisibilityTest`, and `HibernateValidationOrderTest`: inherited/replaced/removed annotations, visible and hidden scalar/object/list properties, repeated/inverse dependencies, display-name lookup, edit-context cleanup, and successful/failing recursive validation through default groups, explicit groups, and sequences. |
| `validator.internal.engine.constraintvalidation.ConstraintTree` | Validate the shape of interpolative settings before runtime variables are available: substitute `@Interpolative.exampleVar` and unescape `@@` before other validators run. Apply this to strings and collections, while leaving syntax errors to `InterpolativeValidator`. Introduced with #222 in `0dd7555781`; the real create-tag scenario was added in #1242 (`52dca1aab9`). | `HibernateValidationInterpolationTest`: real `CreateTagStep` names, escaped at signs, custom example values, invalid literals, malformed interpolation, null values, and collections. Assertions also ensure validation does not modify the bean or the reported invalid value. |
| `validator.internal.metadata.core.MetaConstraint` | Expose the active constraint's getter metadata to `ConstraintTree` with a thread-local stack. Added alongside interpolation in `0dd7555781`; nested validation must restore the previous constraint and exceptions must clean it up. | `HibernateValidationInterpolationTest`: interpolation through the public validator API, nested validation, exception cleanup, and isolation between concurrent validation threads. |
| `validator.internal.engine.valuecontext.BeanValueContext` | Share processed-constraint bookkeeping within one validation context tree. Hibernate 9 otherwise repeats cascaded constraints across group sequences when OneDev separates property, cascade, and class validation. The map is confined to one validation call and is discarded with its context. | `HibernateValidationOrderTest`: nested class validators run once across a group sequence; existing concurrent and nested validation tests remain in place. |

The validation tests are in `server-core/src/test/java/io/onedev/server/validation`.
They use a real
`ValidatorFactory` and small beans so they need neither a running OneDev server
nor a database. These behaviors intentionally differ from standard Bean Validation.

`HibernateValidationContainerCascadeTest` also covers cascades declared on getter
return type arguments, including nested containers and inherited/redeclared/removed
cascades. The override check in `ValidatorImpl` recognizes these annotations so
`BranchProtection#getFileProtections()` can use `List<@Valid FileProtection>` without
losing property or class validation of its entries.

## Consistent validation across entry points

The inheritance and ordering tests enforce these rules in `ValidatorImpl`:

- A child annotation replaces the parent in property metadata, `validateValue`,
  and bean/property validation, including the default group. Overriding a getter
  removes its parent's getter constraints; annotations redeclared on the child
  use the child's attributes. Getters inherited without an override retain their
  constraints.
- `@ClassValidating` runs after properties and nested beans and is skipped when
  earlier validation has reported a violation, for both default and explicit
  non-default groups. Sequences stop after a failing group.

The tests include ordinary validation without explicit groups, as well as explicit
groups and group sequences. These rules replace the previous default-group behavior
that also checked overridden parent attributes and ran class validators after failures.
