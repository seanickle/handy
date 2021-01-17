### Dependencies and the repl
* It appears adding new dependencies into the `build.boot` file, and then running `boot local repl` again, 
  downloads required dependencies and makes them useable for in the project. 
* directory structure for a project 

  ```
  my-project-root/
      VERSION
      build.boot
      src/
          blah/
              foo.clj
              blarth.clj
  ```
