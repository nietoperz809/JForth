package jforth;

public interface HandleDFKW {
    void apply (Object o);
    // dStack.push(num); interpret
    // wordBeingDefined.addWord(new Literal(num)); compile
}
