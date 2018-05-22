package routine;

import javafx.util.Pair;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;

import static routine.RuleOperations.*;
import static routine.RuleOperations.hasCommonActions;
import static routine.RuleOperations.isCover;
import static routine.RuleParser.parseClassifier;

public class Optimizator {

    public static ArrayList<Rule> optimizeClassifier(ArrayList<Rule> classifier)
    {
//        long time = System.currentTimeMillis();
//        System.err.println(classifier.size());
        classifier = applyAllForwardSubsumtions(classifier);
//        System.err.println(classifier.size());
        classifier = applyResolutions(classifier);
//        System.err.println(classifier.size());
//        System.err.println("Time: "+(System.currentTimeMillis() - time));
        classifier = applyAllBackwardSubsumtions(classifier);
 //       System.err.println("Time: "+(System.currentTimeMillis() - time));
        System.err.println(classifier.size());
        return classifier;
    }

    public static ArrayList<Rule> applyAllForwardSubsumtionsForRule(ArrayList<Rule> classifier, int p)
    {
        boolean[] isRemove = new boolean[classifier.size()];
        for (int i = p+1; i < classifier.size(); i++)
        {
            if (isCover(classifier.get(p), classifier.get(i)))
            {
                isRemove[i] = true;
            }
        }
        return ClassifierConstruct.clear(classifier, isRemove);
    }

    public static ArrayList<Rule> applyAllForwardSubsumtions(ArrayList<Rule> classifier)
    {
        for (int i = 0; i < classifier.size(); i++)
        {
            classifier = applyAllForwardSubsumtionsForRule(classifier, i);
        }
        return classifier;
    }


    static class PairInt
    {
        public PairInt(int i, int j)
        {
            this.i = i;
            this.j = j;
        }

        int i;
        int j;
    }

    public static ArrayList<Rule> applyResolutions(ArrayList<Rule> classifier)
    {
        ArrayList<PairInt> classifierPairs = new ArrayList<>();
        for (int i = 0; i < classifier.size(); i++)
        {
            for (int j = i+1; j < classifier.size(); j++)
            {
                if (isResolute(classifier.get(i), classifier.get(j)))
                {
                    classifierPairs.add(new PairInt(i,j));
                }
            }
        }

        while (true)
        {
            boolean applied = false;
            for (PairInt p : classifierPairs)
            {
                int i = p.i;
                int j = p.j;
                if (!isResolute(classifier.get(i), classifier.get(j)))
                {
                    throw new AssertionError();
                }

                ArrayList<Rule> intersectingR2 = new ArrayList<>();
                for (int t = i+1; t < j - 1; t++)
                {
                    if (isIntersect(classifier.get(t), classifier.get(j)))
                    {
                        intersectingR2.add(classifier.get(t));
                    }
                }

                Rule nw = createRuleByResolution(classifier.get(i), classifier.get(j), intersectingR2);
                if (nw == null)
                {
                    continue;
                }
                classifier.set(i, nw);

                HashSet<Integer> removeIndices = new HashSet<>();
                for (int t = i+1; t < classifier.size(); t++)
                {
                    if (isCover(classifier.get(i), classifier.get(t)))
                    {
                        int finalT = t;
                        classifierPairs.removeIf((o1) -> o1.i == finalT ||  o1.j == finalT);
                        removeIndices.add(t);
                    }
                }
                classifier = applyAllForwardSubsumtionsForRule(classifier, i);

                classifierPairs.removeIf((o1) -> o1.i == i || o1.j == i);

                for (PairInt t : classifierPairs)
                {
                    int cntI = 0;
                    int cntJ = 0;
                    for (int c : removeIndices)
                    {
                        if (c == t.i || c == t.j)
                        {
                            throw new AssertionError();
                        }
                        if (c < t.i)
                        {
                            cntI++;
                        }
                        if (c < t.j)
                        {
                            cntJ++;
                        }
                    }
                    t.i -= cntI;
                    t.j -= cntJ;
                }

                for (int t = 0; t < classifier.size(); t++)
                {
                    if (t != i && isResolute(classifier.get(t), classifier.get(i)))
                    {
                        classifierPairs.add(new PairInt(t < i ? t : i, t >i ? t :i));
                    }
                }

                Collections.sort(classifierPairs,(o1, o2) -> {
                    if (o1.i != o2.i)
                    {
                        return Integer.compare(o1.j, o2.j);
                    }
                    return Integer.compare(o1.i, o2.i);
                });
                applied = true;
                break;
            }
            if (!applied)
            {
                break;
            }
        }

        return classifier;
    }

    public static ArrayList<Rule> applyAllBackwardSubsumtions(ArrayList<Rule> rules)
    {
        for (int i = rules.size() -1; i >0; i--)
        {
            ArrayList<Rule> intersectingRules = new ArrayList<>();
            for (int j = i+1; j < rules.size(); j++)
            {
                if (!isIntersect(rules.get(j), rules.get(i))) {
                    continue;
                }

                if (!hasCommonActions(rules.get(i), rules.get(j)))
                {
                    break;
                }

                intersectingRules.add(rules.get(j));

                if (isCover(rules.get(j), rules.get(i)))
                {
                    for (Rule r : intersectingRules)
                    {
                        r.actions.retainAll(rules.get(i).actions);
                    }
                    rules.remove(i);
                    break;
                }
            }
        }
        return rules;
    }

}
