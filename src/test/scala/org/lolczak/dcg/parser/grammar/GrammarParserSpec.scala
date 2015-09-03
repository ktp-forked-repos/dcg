package org.lolczak.dcg.parser.grammar

import org.lolczak.dcg.Grammar._
import org.lolczak.dcg._
import org.lolczak.dcg.parser.grammar.GrammarParser.{keyword => _, _}
import org.scalatest.{Matchers, WordSpec}

import scala.Predef.{augmentString => _, wrapString => _, _}
import scala.io.Source

class GrammarParserSpec extends WordSpec with Matchers {

  "Term parser" should {
    "parse simple term containing only symbol" in {
      //given
      val termString = "Verb"
      //when
      val result = term(new GrammarParser.lexical.Scanner(termString))
      //then
      result should matchPattern { case Success(Term("Verb", FeatureStruct.empty), _) => }
    }

    "parse term containing feature" in {
      //given
      val termString = "Verb[Per=frst]"
      val ExpectedFStruct = FeatureStruct(Map("Per" -> FConst("frst")))
      //when
      val result = term(new GrammarParser.lexical.Scanner(termString))
      //then
      result should matchPattern { case Success(Term("Verb", ExpectedFStruct), _) => }
    }

    "parse term containing features" in {
      //given
      val termString = "Verb[Per=frst, Num=?n, Cas=Acc]"
      val ExpectedFStruct = FeatureStruct(Map("Per" -> FConst("frst"), "Num" -> FVariable("n"), "Cas" -> FConst("Acc")))
      //when
      val result = term(new GrammarParser.lexical.Scanner(termString))
      //then
      result should matchPattern { case Success(Term("Verb", ExpectedFStruct), _) => }
    }

  }

  "Nonterminal production parser" should {
    "parse simple nonterminal production" in {
      //given
      val nonterminalString = "S -> NP VP"
      val ExpectedProduction = "S" ~>("NP", "VP")
      //when
      val result = nonterminal(new GrammarParser.lexical.Scanner(nonterminalString))
      //then
      result should matchPattern { case Success(ExpectedProduction, _) => }
    }

    "parse nonterminal production containing features" in {
      //given
      val nonterminalString = "NP[Num=?n, Gen=male] -> Det[Num=?n] Noun[Num=?n] "
      val ExpectedProduction = "NP"("Num" -> FVariable("n"), "Gen" -> FConst("male")) ~>("Det"("Num" -> FVariable("n")), "Noun"("Num" -> FVariable("n")))
      //when
      val result = nonterminal(new GrammarParser.lexical.Scanner(nonterminalString))
      //then
      result should matchPattern { case Success(ExpectedProduction, _) => }
    }

  }

  "Terminal production parser" should {
    "parse simple terminal production" in {
      //given
      val terminalString = "Noun -> 'test'"
      val ExpectedProduction = LexProduction("Noun", List("test"))
      //when
      val result = terminal(new GrammarParser.lexical.Scanner(terminalString))
      //then
      result should matchPattern { case Success(ExpectedProduction, _) => }
    }

    "parse feature based terminal production" in {
      //given
      val terminalString = "Noun[Num=sg, Gen=male] -> 'test' | 'test2' "
      val ExpectedProduction = LexProduction("Noun"("Num" -> FConst("sg"), "Gen" -> FConst("male")), List("test", "test2"))
      //when
      val result = terminal(new GrammarParser.lexical.Scanner(terminalString))
      //then
      result should matchPattern { case Success(ExpectedProduction, _) => }
    }

  }

  "Grammar parser" should {

    "parse whole grammar" in {
      //given
      val grammarString = Source.fromURL(Thread.currentThread().getContextClassLoader.getResource("feature_based_gram.dcg"), "UTF-8").mkString
      val ExpectedLexicon = new Lexicon(
        "fly" -> Set[Term]("Verb", "Noun"("Num" -> FConst("sg"))),
        "like" -> Set[Term]("Verb", "Prep"),
        "arrow" -> Set[Term]("Noun"("Num" -> FConst("sg"))),
        "these" -> Set[Term]("Det"("Num" -> FConst("pl"))),
        "planes" -> Set[Term]("Noun"("Num" -> FConst("pl"))),
        "an" -> Set[Term]("Det"("Num" -> FConst("sg")))
      )
      val ExpectedGrammar = Grammar("S",
        List(
          "S" ~>("NP", "VP"),
          "VP" ~> "Verb",
          "VP" ~>("Verb", "NP"),
          "VP" ~>("VP", "PP"),
          "NP"("Num" -> FVariable("n")) ~> "Noun"("Num" -> FVariable("n")),
          "NP"("Num" -> FVariable("n")) ~>("Det"("Num" -> FVariable("n")), "Noun"("Num" -> FVariable("n"))),
          "NP"("Num" -> FVariable("n")) ~>("NP"("Num" -> FVariable("n")), "PP"),
          "PP" ~>("Prep", "NP")
        )
      )
      //when
      val result = parseGrammar(grammarString)
      //then
      result should matchPattern { case Success((ExpectedLexicon, ExpectedGrammar), _) => }
    }

  }

}