import routine.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.*;

import static routine.RuleParser.*;
import static routine.RuleOperations.*;

public class Main {

    public ArrayList<Rule> create(ArrayList<Rule> rules, double p, boolean isShuffle) {
        ArrayList<Rule> answer = new ArrayList<>();
        for (Rule r : rules) {
            answer.add(new Rule(r));
        }

        ArrayList<Rule> answer2 = new ArrayList<>(answer);

        if (isShuffle) {
            Collections.shuffle(answer, new Random(239));
        }

        int l = (int) (0.33 * rules.size());
        int r = (int) (0.66 * rules.size());


        for (int i = 0; i < rules.size(); i++) {
            int action = 0;
            if (i > 0.33 * rules.size()) {
                action = 1;
            }
            if (i > 0.66 * rules.size()) {
                action = 2;
            }
            answer.get(i).actions.add(action);
        }

        if (p < 1e-6)
        {
            return answer2;
        }

        int l1 = (int) (0.33 * rules.size() - 0.33 * 0.5 * p * rules.size());
        int r1 = (int) (0.33 * rules.size() + 0.33 * 0.5 * p * rules.size());

        int l2 = (int) (0.66 * rules.size() - 0.33 * 0.5 * p * rules.size());
        int r2 = (int) (0.66 * rules.size() + 0.33 * 0.5 * p * rules.size());

        for (int i = l1; i < r1; i++) {
            answer.get(i).actions.add(0);
            answer.get(i).actions.add(1);
        }

        for (int i = l2; i < r2; i++) {
            answer.get(i).actions.add(1);
            answer.get(i).actions.add(2);
        }


        return answer2;
    }

    public ArrayList<Rule> createRand(ArrayList<Rule> rules, int k, boolean isShuffle) {
        ArrayList<Rule> answer = new ArrayList<>();
        for (Rule r : rules) {
            answer.add(new Rule(r));
        }

        ArrayList<Rule> answer2 = new ArrayList<>(answer);

        if (isShuffle) {
            Collections.shuffle(answer, new Random(239));
        }

        int l = (int) (0.33 * rules.size());
        int r = (int) (0.66 * rules.size());


        for (int i = 0; i < rules.size(); i++) {
            int action = 0;
            if (i > 0.33 * rules.size()) {
                action = 1;
            }
            if (i > 0.66 * rules.size()) {
                action = 2;
            }
            answer.get(i).actions.add(action);
        }

        Random rnd = new Random();
        for (int i = 0; i < answer.size() / 100 * k; i++) {
            int rn = rnd.nextInt(answer.size());
            answer.get(rn).actions.add(0);
            answer.get(rn).actions.add(1);
            answer.get(rn).actions.add(2);
        }
        return answer2;
    }

    public ArrayList<Rule> clearFromOld(ArrayList<Rule> classifier, int val) {
        for (int i = classifier.size() - 1; i >= 0; i--) {
            int cnt = 0;
            for (int j = i - 1; j >= 0; j--) {
                if (isCover(classifier.get(i), classifier.get(j))) {
                    cnt++;
                }
            }
            if (cnt > val) {
                classifier.remove(i);
            }
        }
        return classifier;

    }

    public void testClassifier(ArrayList<Rule> forAnalyze) {
        int med = forAnalyze.size() / 6;
        int[] ans = new int[10];
        int cntR = 0;
        for (int i = 0; i < forAnalyze.size(); i++) {
            for (int j = i + 1; j < forAnalyze.size(); j++) {
                if (isCover(forAnalyze.get(j), forAnalyze.get(i))) {
                    ans[j / med]++;
                    break;
                }
                if (isResolute(forAnalyze.get(i), forAnalyze.get(j))) {
                    cntR++;
                }

            }
        }
        System.err.println(Arrays.toString(ans) + " " + cntR);

    }

    public void runGeneral(String... filename) throws FileNotFoundException {
        ArrayList<Rule> classifier = RuleParser.parseClassifier(filename);
        System.err.println(classifier.size());
        classifier = ClassifierConstruct.cutWithManyStars(classifier);
        System.err.println(classifier.size());
//        classifier = ClassifierConstruct.leaveOnlyInOperations(classifier);
//        System.err.println(classifier.size());
        PrintWriter out = new PrintWriter("ans_"+filename[0]);
        out.println(classifier.size());
        ArrayList<Rule> classiferTemp = createRand(classifier, 0, false);
        int cntOrig = Optimizator.optimizeClassifier(classiferTemp).size();
        ArrayList<Rule> classiferTemp2 = createRand(classifier, 0, true);
        int cntOrig2 = Optimizator.optimizeClassifier(classiferTemp2).size();
        ArrayList<Rule> classifierBest = ClassifierConstruct.makeSameAction(classifier);
        int cntBest = Optimizator.optimizeClassifier(classifierBest).size();

        out.println(cntOrig +" "+cntOrig2 +" "+cntBest);

        for (int k = 1; k <= 10; k++) {
            ArrayList<Rule> classifierNew = create(classifier, 0.1 * k, false);
            classifierNew = Optimizator.optimizeClassifier(classifierNew);
            ArrayList classifierNewAnother = create(classifier, 0.1 * k, true);
            classifierNewAnother = Optimizator.optimizeClassifier(classifierNewAnother);
            ArrayList classifierNewAnother2 = createRand(classifier, 3 * k, false);
            classifierNewAnother2 = Optimizator.optimizeClassifier(classifierNewAnother2);
            ArrayList classifierNewAnother23 = createRand(classifier, 3 * k, true);
            classifierNewAnother23 = Optimizator.optimizeClassifier(classifierNewAnother23);

            out.printf("%d : %d %.3f %d %.3f %d %.3f %d %.3f\n", k,
                    classifierNew.size(),
                    1.0 * (cntOrig - classifierNew.size()) / (cntOrig - cntBest),
                    classifierNewAnother.size(),
                    1.0 * (cntOrig2 - classifierNewAnother.size()) / (cntOrig2 - cntBest),
                    classifierNewAnother2.size(),
                    1.0 * (cntOrig - classifierNewAnother2.size()) / (cntOrig - cntBest),
                    classifierNewAnother23.size(),
                    1.0 * (cntOrig2 - classifierNewAnother23.size()) / (cntOrig2 - cntBest)
            );
            out.flush();
        }
        out.close();

    }

    public void runFib(File f) throws FileNotFoundException, UnknownHostException {
        PrintWriter out = new PrintWriter("ans_"+f.getName());
        System.err.println(f.getName());
        ArrayList<Rule> classifer = RuleParser.parseIpClassifier(f);
        System.err.println(classifer.size());
        classifer =  ClassifierConstruct.clearObviousWaste(classifer);
        System.err.println(classifer.size());
        int LPMExact = new LPMSolver().calculateNumberOfRules(classifer);
        ArrayList<Rule> copy = ClassifierConstruct.giveAlternativesForFibClassifier(classifer, -1e-5, 0);
        int FibExact = FibOptimizator.optimize(copy).size();
        out.println(LPMExact+" "+FibExact);
        out.flush();
        ArrayList<Rule> bestOne = ClassifierConstruct.makeSameAction(classifer);
        int LPMBest = new LPMSolver().calculateNumberOfRules(bestOne);
        int FibBest = FibOptimizator.optimize(bestOne).size();
        out.println(LPMBest+" "+FibBest);
        out.flush();
        for (int i = 0; i <= 10; i++)
        {
            out.print(i+" : ");
            for (int it = 1; it <= 3; it++)
            {
                ArrayList<Rule> cand1 = ClassifierConstruct.giveAlternativesForFibClassifier(classifer, i*0.1,it);
                int LPMAppAns1 = new LPMSolver().calculateNumberOfRules(cand1);
                ArrayList<Rule> cand2 = FibOptimizator.optimize(cand1);

                String ansik = String.format("%d %.3f %.3f %d %.3f %.3f ", LPMAppAns1, 1.0*(LPMExact-LPMAppAns1)/LPMExact, 1.0*(LPMExact-LPMAppAns1)/(LPMExact - LPMBest), cand2.size(), 1.0*(FibExact - cand2.size())/FibExact, 1.0*(FibExact - cand2.size())/(FibExact - FibBest));
                out.print(ansik+" ");
                out.flush();
            }
            out.println();
        }
        out.close();
    }


    public static void main(String[] args) throws FileNotFoundException, UnknownHostException {
        if (args[0].equals("fib"))
        {
            new Main().runFib(new File(args[1]));
            return;
        }
        if (args[0].equals("gen"))
        {
            new Main().runGeneral(args[1]);
            return;
        }
    }


}
