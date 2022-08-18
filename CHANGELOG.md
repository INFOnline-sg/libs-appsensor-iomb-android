## 1.0.3 (2022-08-18)

**Changed**

- Optimised data validation routine


## 1.0.2 (2022-03-04)

**Changed**

- Updated all relevant dependencies
- Default value for empty category
- Optimised maven publishing (gradle plugin)
- Optimised obfuscation
- Compile/target SDK level 31

**Fixed**

- Category string sanitizing
- Scope of transitive dependencies in Maven POM (avoid timber lint warnings)
- Potential NPE in network state handling

## 1.0.1 (2021-11-19)

**Changed**

- Using region of measurement instead of device region settings ("cn")

**Fixed**

- Character encoding/escaping for comment parameter ("co")
- Log output with debugMode=true


## 1.0.0 (2021-10-14)

- Initial release