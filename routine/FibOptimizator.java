package routine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import static routine.RuleOperations.*;
import static routine.RuleOperations.hasCommonActions;

public class FibOptimizator {

    public static ArrayList<Rule> optimize(ArrayList<Rule> ans)
    {
        long tm = System.currentTimeMillis();
        for (int it = 0;; it++)
        {
            int old = ans.size();
            ans = applyAllFibForwardSubsumptions(ans);
            ans = applyAllFibResolutions(ans, it == 0);
            if (ans.size() == old)
            {
                break;
            }
        }
        ans = applyAllFibBackwardSubsumptions(ans);
        System.err.println(System.currentTimeMillis() - tm);
        return ans;
    }


    public static ArrayList<Rule> applyAllFibForwardSubsumptions(ArrayList<Rule> ans)
    {
        HashSet<String> was = new HashSet<>();
        ArrayList<Rule>[] satisfied = new ArrayList[ans.get(0).bits.length+3];
        for (int i = 0; i < satisfied.length; i++)
        {
            satisfied[i] = new ArrayList<>();
        }
        boolean[] toRemove = new boolean[ans.size()];
        for (int i = 0; i < ans.size(); i++)
        {
            if (was.contains(ans.get(i).toBitString()))
            {
                toRemove[i] = true;
                continue;
            }
            for (int j = ans.get(i).stars+1; j< satisfied.length; j++)
            {
                for (Rule k : satisfied[j])
                {
                    if (isCover(k, ans.get(i)))
                    {
                        toRemove[i] = true;
                        break;
                    }
                }
                if (toRemove[i])
                {
                    break;
                }
            }
            if (!toRemove[i])
            {
                satisfied[ans.get(i).stars].add(ans.get(i));
                was.add(ans.get(i).toBitString());
            }
        }

        ans = ClassifierConstruct.clear(ans, toRemove);
        return ans;

    }

    public static ArrayList<Rule> applyAllFibBackwardSubsumptions(ArrayList<Rule> ans)
    {
        for (int i = 0; i < ans.size(); i++)
        {
            ans.get(i).prioroty = i;
        }
        ArrayList<Rule>[][] satisfied =  new ArrayList[ans.get(0).bits.length+3][ans.get(0).bits.length+3];
        for (int i = 0; i < satisfied.length; i++)
        {
            for (int j = 0; j < satisfied[i].length; j++)
            {
                satisfied[i][j] = new ArrayList<>();
            }
        }

        boolean[] toRemove = new boolean[ans.size()];
        for (int i = ans.size() - 1; i >= 0; --i)
        {
            Rule r = ans.get(i);
            Rule covering = null;
            for (int j = r.stars+1; j <  satisfied.length; j++)
            {
                for (int k = Math.max(0, r.zeros - (j-r.stars)); k <= r.zeros; k++)
                {
                    for (Rule z : satisfied[j][k])
                    {
                        if (toRemove[z.prioroty] || !isCover(z, r))
                        {
                            continue;
                        }
                        if (covering == null || covering.prioroty > z.prioroty)
                        {
                            covering = z;
                        }
                    }
                }
            }
            if (covering == null)
            {
                satisfied[r.stars][r.zeros].add(r);
                continue;
            }
            toRemove[i] = true;
            for (int j = i+1; j<= covering.prioroty; j++)
            {
                if (toRemove[j] || !isIntersect(r, ans.get(j)))
                {
                    continue;
                }
                toRemove[i] &= hasCommonActions(r, ans.get(j));
            }

            if (toRemove[i])
            {
                for (int j = i+1; j<= covering.prioroty; j++)
                {
                    if (isIntersect(r, ans.get(j)))
                    {
                        ans.get(j).actions.retainAll(r.actions);
                    }
                }
            }
            else
            {
                satisfied[r.stars][r.zeros].add(r);
            }
        }

        return ClassifierConstruct.clear(ans, toRemove);
    }


    public static ArrayList<Rule> applyAllFibResolutions(ArrayList<Rule> ans, boolean shouldCheck)
    {
        for (int i = 0; i < ans.size(); i++)
        {
            ans.get(i).prioroty = i;
        }

        ArrayList<Rule>[][] satisfied = new ArrayList[ans.get(0).bits.length+3][ans.get(0).bits.length+3];
        boolean[] used = new boolean[ans.size()];
        boolean[] toRemove = new boolean[ans.size()];

        for (int i = 0; i < satisfied.length; i++)
        {
            for (int j = 0; j < satisfied[0].length; j++) {
                satisfied[i][j] = new ArrayList<>();
            }
        }

        for (Rule r : ans)
        {
            ArrayList<Rule> resolutingRules = new ArrayList<>();

            int k = r.stars;
            for (int t = r.zeros - 1; t <= r.zeros + 1; t++)
            {
                if (t < 0)
                {
                    continue;
                }
                for (Rule z : satisfied[k][t])
                {
                    if (used[z.prioroty] || !isResolute(z, r))
                    {
                        continue;
                    }

                    Rule cand = createRuleByResolution(z, r, new ArrayList<>());
                    if (cand == null)
                    {
                        continue;
                    }
                    resolutingRules.add(cand);
                }
            }
            if (resolutingRules.size() == 0)
            {
                satisfied[r.stars][r.zeros].add(r);
                continue;
            }

            Collections.sort(resolutingRules, (o1, o2) -> o2.prioroty - o1.prioroty);

            Rule resolutedRule = !shouldCheck ? resolutingRules.get(0) : null;

            HashSet<Integer> curActions = new HashSet<>(r.actions);
            int j = r.prioroty - 1;
            for (int i = 0; i < resolutingRules.size() && resolutedRule == null && curActions.size() > 0; i++)
            {
                while (j > resolutingRules.get(i).prioroty && curActions.size() > 0)
                {
                    if (!toRemove[j] && isIntersect(r, ans.get(j)))
                    {
                        curActions.retainAll(ans.get(j).actions);
                    }
                    j--;
                }
                if (resolutingRules.get(i).actions.stream().anyMatch(curActions::contains))
                {
                    resolutedRule = resolutingRules.get(i);
                    resolutingRules.get(i).actions.retainAll(curActions);
                }
            }

            if (resolutedRule == null)
            {
                continue;
            }

            ans.set(resolutedRule.prioroty, resolutedRule);
            used[resolutedRule.prioroty] = true;
            used[r.prioroty] = true;
            toRemove[r.prioroty] = true;

        }
        return ClassifierConstruct.clear(ans, toRemove);
    }



}
