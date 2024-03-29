/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package methods;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.Arrays;
import java.util.UUID;

/**
 *
 * @author anu
 */
public class policy {

    private String policyName;
    private double[] objectives;
    private double score;
    private int dominated;
    private double distance;
    private int dominatedbycategory;
    private String order;
    private String Si;
    private String SiR;
    private int rank;
    private int ni;
    private static int uniqueid = 1;
    private String weights;

    public policy(int number, boolean hasname) {
        this.objectives = new double[number];
        this.score = 0;
        this.dominated = 0;
        this.distance = 0;
        this.dominatedbycategory = 0;
        this.order = "";
        this.Si = "";
        this.SiR = "";
        this.rank = 0;
        this.ni = 0;
        this.weights = "";
        if (!hasname) {
            //  policyName = UUID.randomUUID().toString();
            policyName = Integer.toString(uniqueid);
            uniqueid++;
        } else {
            policyName = "";
        }
    }

    public String getSiR() {
        return SiR;
    }

    public void setSiR(String SiR) {
         if (this.SiR.equals("")) {
            this.SiR = SiR;
        } else {
            this.SiR = SiR + " , " + this.SiR;
        }
    }

    public String getWeights() {
        return weights;
    }

    public void setWeights(String weights) {
        this.weights = weights;
    }

    public void changewithWeights() {
        if (!weights.equals("")) {
            String[] wei = weights.split(",");
            for (int i = 0; i < objectives.length; i++) {
                objectives[i] = objectives[i] * Double.parseDouble(wei[i]);
            }
        }
    }

    public int getNi() {
        return ni;
    }

    public void setNi(int ni) {
        this.ni = ni;
    }

    public String getSi() {
        return Si;
    }

    public void setSi(String Si) {
        if (this.Si.equals("")) {
            this.Si = Si;
        } else {
            this.Si = Si + " , " + this.Si;
        }
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getDominated() {
        return dominated;
    }

    public void setDominated(int dominated) {
        this.dominated = dominated;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public double[] getObjectives() {
        return objectives;
    }

    public void setObjectives(double[] objectives) {
        this.objectives = objectives;
    }

    public void setDistance() {

        //euclidean 
        double sum = 0;
        for (int i = 0; i < objectives.length; i++) {
            sum += Math.pow(objectives[i], 2);
        }
        this.distance = (double)Math.round(Math.sqrt(sum) * 10000) / 10000;
    }

    public int getDominatedbycategory() {
        return dominatedbycategory;
    }

    public void setDominatedbycategory(int dominatedbycategory) {
        this.dominatedbycategory = dominatedbycategory;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(int objectives_number) {
        //in order to create the right order we need to substract from the total number of objectives
        int[] thisorder = new int[objectives.length];
        double[] sorted = objectives.clone();
        Arrays.sort(sorted);
        String myorder = "";
        //TODO fix O() add equals in same order value
        for (int j = 0; j < objectives.length; j++) {
            for (int i = 0; i < objectives.length; i++) {

                if (objectives[j] == sorted[i]) {
                    thisorder[j] = objectives_number - i;
                }
            }
        }

        for (int u = 0; u < thisorder.length; u++) {
            myorder += thisorder[u];
        }
        this.order = myorder;
    }

}
