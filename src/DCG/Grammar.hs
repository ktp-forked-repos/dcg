module DCG.Grammar where

import qualified Data.Map as Map

data Grammar = Grammar {topTerm :: String, productions :: [Production]} deriving (Eq, Show)

data Production = Production {lhs :: Term, rhs :: [Term]}
--                | Terminal Term [String]
                deriving (Eq, Ord, Show)

data Term = Term {name :: String} deriving (Eq, Ord, Show)

--data Rhs = Seq deriving (Eq, Ord, Show)

type Lexicon = Map.Map String [Term]

findInLexicon :: Lexicon -> String -> Maybe [Term]
findInLexicon l w = Map.lookup w l

infix 9 ==>
(==>) :: String -> [String] -> Production
lhs ==> rhs = Production (Term lhs) $ map Term rhs

infix 9 ~~>
(~~>) :: Term -> [String] -> Lexicon
lhs ~~> rhs = error "Not implemented"

validate :: Grammar -> Bool
validate _ = True