# hypervolume


Repository holds the code to repoduce thr experiments detailed in:


Jonathan E. Fieldsend. 2019. 
Efficient Real-Time Hypervolume Estimation with Monotonically Reducing Error. 
In Genetic and Evolutionary Computation Conference (GECCO ’19), 
July 13–17, 2019, Prague, Czech Republic. ACM, New York, NY, USA

Institutional Repository: http://hdl.handle.net/10871/36825

Publisher DOI: https://doi.org/10.1145/3321707.3321730

The main class for the experiments is the imaginatively titled Main.java

After compliation, running the class without arguments will detail what is expected.

```
>> java Main

Not enough input arguments, four arguments expected:
 DTLZ problem number (1 OR 2),
 Hypervolume estimate update type  (B, I, S OR D),
 number of iterations (minimum 0 applied),
 number of objectives (minumum 2 applied) and
 number of folds (minimum 1 appied)
```

An example run would be 

```
java Main 1 B 1000 4 10
```

Which would use DTLZ2, the basic MC update (here 5000 samples each iteration, I will make this adjustable in revisions), for 1000 iterations of the (1+1)-ES, in 4 objective dimensions, for 10 folds.
