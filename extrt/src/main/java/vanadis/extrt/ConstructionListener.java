package vanadis.extrt;

public interface ConstructionListener {

    void constructionTimeAgain(ConstructorGatherer gatherer);

    void destructionTimeAgain(ConstructorGatherer gatherer);
}
