package routine;

import java.util.*;

import static routine.RuleOperations.*;
import static routine.RuleOperations.isIntersect;

public class ClassifierConstruct {
    public static ArrayList<Rule> cutWithManyStars(ArrayList<Rule> classifier) {
        int manyStars = 69;
        for (int i = classifier.size() - 1; i >= 0; --i) {
            if (classifier.get(i).stars > manyStars) {
                classifier.remove(i);
            }
        }
        return classifier;
    }

    public static ArrayList<Rule> clear(ArrayList<Rule> classifier, boolean[] isRemove) {
        ArrayList<Rule> resultClassifier = new ArrayList<>();
        for (int i = 0; i < isRemove.length; i++) {
            if (!isRemove[i]) {
                resultClassifier.add(classifier.get(i));
            }
        }
        return resultClassifier;
    }


    public static ArrayList<Rule> leaveOnlyInOperations(ArrayList<Rule> classifier) {
        ArrayList<Rule> rulesAfterAllPossibleResolution = new ArrayList<>();

        for (int i = 0; i < classifier.size(); i++) {
            for (int j = i + 1; j < classifier.size(); j++) {
                if (isResolute(classifier.get(i), classifier.get(j)))
                    rulesAfterAllPossibleResolution.add(createRuleByResolutionWithoutActions(classifier.get(i), classifier.get(j)));
            }
        }

        System.err.println(rulesAfterAllPossibleResolution.size());

        for (int i = 0; i < rulesAfterAllPossibleResolution.size(); i++) {
            for (int j = 0; j < classifier.size(); j++) {
                if (isResolute(classifier.get(j), rulesAfterAllPossibleResolution.get(i))) {
                    rulesAfterAllPossibleResolution.add(createRuleByResolutionWithoutActions(classifier.get(j), rulesAfterAllPossibleResolution.get(i)));
                }
            }
            for (int j = 0; j < i; j++) {
                if (isResolute(rulesAfterAllPossibleResolution.get(j), rulesAfterAllPossibleResolution.get(i))) {
                    rulesAfterAllPossibleResolution.add(createRuleByResolutionWithoutActions(rulesAfterAllPossibleResolution.get(j), rulesAfterAllPossibleResolution.get(i)));
                }
            }
        }

        boolean[] isAppearInOperation = new boolean[classifier.size()];
        for (int i = 0; i < classifier.size(); i++) {
            for (int j = i + 1; j < classifier.size(); j++) {
                if (isCover(classifier.get(i), classifier.get(j))) {
                    isAppearInOperation[i] = true;
                    isAppearInOperation[j] = true;
                }
                if (isCover(classifier.get(j), classifier.get(i))) {
                    isAppearInOperation[i] = true;
                    isAppearInOperation[j] = true;
                }
            }
        }

        boolean[] shouldRemove = new boolean[classifier.size()];
        Arrays.fill(shouldRemove, true);

        for (int i = 0; i < classifier.size(); i++) {
            if (!isAppearInOperation[i]) {
                continue;
            }
            for (int j = 0; j < classifier.size(); j++) {
                if (isIntersect(classifier.get(i), classifier.get(j))) {
                    shouldRemove[j] = false;
                }
            }
        }
        for (Rule r : rulesAfterAllPossibleResolution) {
            for (int i = 0; i < classifier.size(); i++) {
                if (isIntersect(r, classifier.get(i))) {
                    shouldRemove[i] = false;
                }
            }
        }

        return clear(classifier, shouldRemove);
    }


    public static ArrayList<Rule> clearObviousWaste(ArrayList<Rule> ans) {
        while (true) {
            HashSet<String> visited = new HashSet<>();
            boolean[] toRemove = new boolean[ans.size()];
            for (int i = 0; i < ans.size(); i++) {
                String strK = ans.get(i).toBitString().substring(0, ans.get(i).bits.length- ans.get(i).stars);
                for (int j = 0; j <= strK.length(); j++) {
                    toRemove[i] |= visited.contains(strK.substring(0, j));
                }
                visited.add(strK);
            }
            ans = ClassifierConstruct.clear(ans, toRemove);

            int cntKR = 0;
            ans.sort(Comparator.comparingInt(o -> o.stars));
            HashMap<String, Rule> wasMapRule = new HashMap<>();
            for (int i = 0; i < ans.size(); i++) {
                String str = ans.get(i).toBitString().substring(0, ans.get(i).bits.length - ans.get(i).stars - 1);
                if (wasMapRule.containsKey(str)) {
                    if (RuleOperations.hasCommonActions(wasMapRule.get(str), ans.get(i))) {
                        if (!RuleOperations.isResolute(wasMapRule.get(str), ans.get(i))) {
                            throw new AssertionError();
                        } else {
                            wasMapRule.get(str).modifyBit('*', str.length());
                            wasMapRule.remove(str);
                            cntKR++;
                        }
                    }
                } else {
                    wasMapRule.put(str, ans.get(i));
                }
            }
            if (cntKR == 0) {
                break;
            }
        }

        HashMap<String, Rule> was = new HashMap<>();
        boolean[] toRemove = new boolean[ans.size()];
        for (int i = ans.size() - 1; i >= 0; --i) {
            Rule r = ans.get(i);
            for (int j = ans.get(i).bits.length - r.stars - 1; j > 0; j--) {
                Rule k = was.get(r.toBitString().substring(0, j));
                if (k != null) {
                    toRemove[i] = RuleOperations.hasCommonActions(r, k);
                    break;
                }
            }
            was.put(r.toBitString().substring(0, ans.get(i).bits.length - r.stars), r);
        }
        ans = ClassifierConstruct.clear(ans, toRemove);
        return ans;
    }

    public static ArrayList<Rule> giveAlternativesForFibClassifier(ArrayList<Rule> classifier, double p, int numA) {

        ArrayList<Rule> ans3 = new ArrayList<>();
        for (Rule r : classifier) {
            Rule nw = new Rule(r);
            nw.actions.addAll(r.actions);
            ans3.add(nw);
        }


        HashSet<Integer> diffActions = new HashSet<>();
        for (Rule r : classifier) {
            diffActions.addAll(r.actions);
        }
        diffActions.remove(-1);
        int[] dist = new int[diffActions.size()+100];
        for (Rule r : classifier) {
            if (r.actions.iterator().next() == -1)
            {
                continue;
            }
            dist[r.actions.iterator().next()]++;
        }
//        System.err.println(Arrays.toString(dist));

        if (ans3.size() == dist[0] + dist[1] + dist[2]) {
            numA = 1;
        }

        Random rnd = new Random();

        for (int i = 0; i < ans3.size(); i++) {
            if (rnd.nextDouble() > p || ans3.get(i).actions.contains(-1)) {
                continue;
            }

            Rule r = ans3.get(i);

            for (int it = 0; it < numA; it++) {
                int sum = 0;
                for (int j = 0; j < dist.length; j++) {
                    if (!r.actions.contains(j)) {
                        sum += dist[j];
                    }
                }

                int k = rnd.nextInt(sum);
                for (int j = 0; j < dist.length; j++) {
                    if (r.actions.contains(j)) {
                        continue;
                    }

                    if (k >= dist[j]) {
                        k -= dist[j];
                    } else {
                        ans3.get(i).actions.add(j);
                        break;
                    }
                }

            }
        }
        return ans3;
    }

    public static ArrayList<Rule> makeSameAction(ArrayList<Rule> classifier) {
        ArrayList<Rule> ans = new ArrayList<>();
        for (Rule r : classifier)
        {
            Rule cl = new Rule(r);
            cl.actions.clear();
            cl.actions.add(0);
            ans.add(cl);
        }
        return ans;
    }
}
