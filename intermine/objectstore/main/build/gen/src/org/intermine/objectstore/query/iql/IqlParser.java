// $ANTLR 2.7.6 (20060327): "intermine_iql.g" -> "IqlParser.java"$

package org.intermine.objectstore.query.iql;

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;
import java.util.IdentityHashMap;
import antlr.collections.AST;
import java.util.Hashtable;
import antlr.ASTFactory;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;

public class IqlParser extends antlr.LLkParser       implements IqlTokenTypes
 {

protected IqlParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public IqlParser(TokenBuffer tokenBuf) {
  this(tokenBuf,7);
}

protected IqlParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public IqlParser(TokenStream lexer) {
  this(lexer,7);
}

public IqlParser(ParserSharedInputState state) {
  super(state,7);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

	IdentityHashMap start_ruleCache = new IdentityHashMap();
	public final void start_rule() throws RecognitionException, TokenStreamException {
		Object retval = start_ruleCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST start_rule_AST = null;
				
				iql_statement();
				astFactory.addASTChild(currentAST, returnAST);
				start_rule_AST = (AST)currentAST.root;
				returnAST = start_rule_AST;
				start_ruleCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				start_ruleCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				start_ruleCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap iql_statementCache = new IdentityHashMap();
	public final void iql_statement() throws RecognitionException, TokenStreamException {
		Object retval = iql_statementCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST iql_statement_AST = null;
				
				select_command();
				astFactory.addASTChild(currentAST, returnAST);
				if ( inputState.guessing==0 ) {
					iql_statement_AST = (AST)currentAST.root;
					iql_statement_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(IQL_STATEMENT,"IQL_STATEMENT")).add(iql_statement_AST));
					currentAST.root = iql_statement_AST;
					currentAST.child = iql_statement_AST!=null &&iql_statement_AST.getFirstChild()!=null ?
						iql_statement_AST.getFirstChild() : iql_statement_AST;
					currentAST.advanceChildToEnd();
				}
				iql_statement_AST = (AST)currentAST.root;
				returnAST = iql_statement_AST;
				iql_statementCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				iql_statementCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				iql_statementCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap select_commandCache = new IdentityHashMap();
	public final void select_command() throws RecognitionException, TokenStreamException {
		Object retval = select_commandCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST select_command_AST = null;
				
				{
				switch ( LA(1)) {
				case LITERAL_explain:
				{
					AST tmp1_AST = null;
					tmp1_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp1_AST);
					match(LITERAL_explain);
					break;
				}
				case LITERAL_select:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				match(LITERAL_select);
				{
				switch ( LA(1)) {
				case LITERAL_all:
				{
					match(LITERAL_all);
					break;
				}
				case LITERAL_distinct:
				{
					AST tmp4_AST = null;
					tmp4_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp4_AST);
					match(LITERAL_distinct);
					break;
				}
				case IDENTIFIER:
				case OPEN_PAREN:
				case LITERAL_bag:
				case INTEGER:
				case LITERAL_bags:
				case QUOTED_STRING:
				case FLOAT:
				case LITERAL_true:
				case LITERAL_false:
				case LITERAL_null:
				case LITERAL_count:
				case LITERAL_max:
				case LITERAL_min:
				case LITERAL_sum:
				case LITERAL_avg:
				case LITERAL_substr:
				case LITERAL_indexof:
				case LITERAL_lower:
				case LITERAL_upper:
				case LITERAL_stddev:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				select_list();
				astFactory.addASTChild(currentAST, returnAST);
				{
				switch ( LA(1)) {
				case LITERAL_from:
				{
					from_list();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case EOF:
				case LITERAL_where:
				case LITERAL_group:
				case LITERAL_order:
				case CLOSE_PAREN:
				case LITERAL_limit:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				{
				switch ( LA(1)) {
				case LITERAL_where:
				{
					where_clause();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case EOF:
				case LITERAL_group:
				case LITERAL_order:
				case CLOSE_PAREN:
				case LITERAL_limit:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				{
				switch ( LA(1)) {
				case LITERAL_group:
				{
					group_clause();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case EOF:
				case LITERAL_order:
				case CLOSE_PAREN:
				case LITERAL_limit:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				{
				switch ( LA(1)) {
				case LITERAL_order:
				{
					order_clause();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case EOF:
				case CLOSE_PAREN:
				case LITERAL_limit:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				select_command_AST = (AST)currentAST.root;
				returnAST = select_command_AST;
				select_commandCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				select_commandCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				select_commandCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap select_listCache = new IdentityHashMap();
	public final void select_list() throws RecognitionException, TokenStreamException {
		Object retval = select_listCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST select_list_AST = null;
				
				select_value();
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop12:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						select_value();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop12;
					}
					
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					select_list_AST = (AST)currentAST.root;
					select_list_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(SELECT_LIST,"SELECT_LIST")).add(select_list_AST));
					currentAST.root = select_list_AST;
					currentAST.child = select_list_AST!=null &&select_list_AST.getFirstChild()!=null ?
						select_list_AST.getFirstChild() : select_list_AST;
					currentAST.advanceChildToEnd();
				}
				select_list_AST = (AST)currentAST.root;
				returnAST = select_list_AST;
				select_listCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				select_listCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				select_listCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap from_listCache = new IdentityHashMap();
	public final void from_list() throws RecognitionException, TokenStreamException {
		Object retval = from_listCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST from_list_AST = null;
				
				match(LITERAL_from);
				abstract_table();
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop15:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						abstract_table();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop15;
					}
					
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					from_list_AST = (AST)currentAST.root;
					from_list_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(FROM_LIST,"FROM_LIST")).add(from_list_AST));
					currentAST.root = from_list_AST;
					currentAST.child = from_list_AST!=null &&from_list_AST.getFirstChild()!=null ?
						from_list_AST.getFirstChild() : from_list_AST;
					currentAST.advanceChildToEnd();
				}
				from_list_AST = (AST)currentAST.root;
				returnAST = from_list_AST;
				from_listCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				from_listCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				from_listCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap where_clauseCache = new IdentityHashMap();
	public final void where_clause() throws RecognitionException, TokenStreamException {
		Object retval = where_clauseCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST where_clause_AST = null;
				
				match(LITERAL_where);
				abstract_constraint();
				astFactory.addASTChild(currentAST, returnAST);
				if ( inputState.guessing==0 ) {
					where_clause_AST = (AST)currentAST.root;
					where_clause_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(WHERE_CLAUSE,"WHERE_CLAUSE")).add(where_clause_AST));
					currentAST.root = where_clause_AST;
					currentAST.child = where_clause_AST!=null &&where_clause_AST.getFirstChild()!=null ?
						where_clause_AST.getFirstChild() : where_clause_AST;
					currentAST.advanceChildToEnd();
				}
				where_clause_AST = (AST)currentAST.root;
				returnAST = where_clause_AST;
				where_clauseCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				where_clauseCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				where_clauseCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap group_clauseCache = new IdentityHashMap();
	public final void group_clause() throws RecognitionException, TokenStreamException {
		Object retval = group_clauseCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST group_clause_AST = null;
				
				match(LITERAL_group);
				match(LITERAL_by);
				abstract_value();
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop19:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						abstract_value();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop19;
					}
					
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					group_clause_AST = (AST)currentAST.root;
					group_clause_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(GROUP_CLAUSE,"GROUP_CLAUSE")).add(group_clause_AST));
					currentAST.root = group_clause_AST;
					currentAST.child = group_clause_AST!=null &&group_clause_AST.getFirstChild()!=null ?
						group_clause_AST.getFirstChild() : group_clause_AST;
					currentAST.advanceChildToEnd();
				}
				group_clause_AST = (AST)currentAST.root;
				returnAST = group_clause_AST;
				group_clauseCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				group_clauseCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				group_clauseCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap order_clauseCache = new IdentityHashMap();
	public final void order_clause() throws RecognitionException, TokenStreamException {
		Object retval = order_clauseCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST order_clause_AST = null;
				
				match(LITERAL_order);
				match(LITERAL_by);
				orderby_value();
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop22:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						orderby_value();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop22;
					}
					
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					order_clause_AST = (AST)currentAST.root;
					order_clause_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(ORDER_CLAUSE,"ORDER_CLAUSE")).add(order_clause_AST));
					currentAST.root = order_clause_AST;
					currentAST.child = order_clause_AST!=null &&order_clause_AST.getFirstChild()!=null ?
						order_clause_AST.getFirstChild() : order_clause_AST;
					currentAST.advanceChildToEnd();
				}
				order_clause_AST = (AST)currentAST.root;
				returnAST = order_clause_AST;
				order_clauseCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				order_clauseCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				order_clauseCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap select_valueCache = new IdentityHashMap();
	public final void select_value() throws RecognitionException, TokenStreamException {
		Object retval = select_valueCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST select_value_AST = null;
				
				{
				switch ( LA(1)) {
				case LITERAL_bag:
				{
					objectstorebag();
					astFactory.addASTChild(currentAST, returnAST);
					{
					_loop34:
					do {
						if (((LA(1) >= LITERAL_union && LA(1) <= LITERAL_allbutintersect))) {
							{
							switch ( LA(1)) {
							case LITERAL_union:
							{
								AST tmp15_AST = null;
								tmp15_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp15_AST);
								match(LITERAL_union);
								break;
							}
							case LITERAL_intersect:
							{
								AST tmp16_AST = null;
								tmp16_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp16_AST);
								match(LITERAL_intersect);
								break;
							}
							case LITERAL_except:
							{
								AST tmp17_AST = null;
								tmp17_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp17_AST);
								match(LITERAL_except);
								break;
							}
							case LITERAL_allbutintersect:
							{
								AST tmp18_AST = null;
								tmp18_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp18_AST);
								match(LITERAL_allbutintersect);
								break;
							}
							default:
							{
								throw new NoViableAltException(LT(1), getFilename());
							}
							}
							}
							objectstorebag();
							astFactory.addASTChild(currentAST, returnAST);
						}
						else {
							break _loop34;
						}
						
					} while (true);
					}
					break;
				}
				case LITERAL_bags:
				{
					bags_for();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				default:
					boolean synPredMatched26 = false;
					if (((_tokenSet_0.member(LA(1))) && (_tokenSet_1.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_3.member(LA(4))) && (_tokenSet_4.member(LA(5))) && (_tokenSet_5.member(LA(6))) && (_tokenSet_6.member(LA(7))))) {
						int _m26 = mark();
						synPredMatched26 = true;
						inputState.guessing++;
						try {
							{
							unsafe_function();
							}
						}
						catch (RecognitionException pe) {
							synPredMatched26 = false;
						}
						rewind(_m26);
inputState.guessing--;
					}
					if ( synPredMatched26 ) {
						unsafe_function();
						astFactory.addASTChild(currentAST, returnAST);
						match(LITERAL_as);
						field_alias();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						boolean synPredMatched28 = false;
						if (((_tokenSet_0.member(LA(1))) && (_tokenSet_7.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_3.member(LA(4))) && (_tokenSet_4.member(LA(5))) && (_tokenSet_5.member(LA(6))) && (_tokenSet_6.member(LA(7))))) {
							int _m28 = mark();
							synPredMatched28 = true;
							inputState.guessing++;
							try {
								{
								typecast();
								}
							}
							catch (RecognitionException pe) {
								synPredMatched28 = false;
							}
							rewind(_m28);
inputState.guessing--;
						}
						if ( synPredMatched28 ) {
							typecast();
							astFactory.addASTChild(currentAST, returnAST);
							match(LITERAL_as);
							field_alias();
							astFactory.addASTChild(currentAST, returnAST);
						}
						else {
							boolean synPredMatched30 = false;
							if (((LA(1)==IDENTIFIER) && (LA(2)==DOT) && (LA(3)==IDENTIFIER) && (LA(4)==OPEN_PAREN) && (_tokenSet_8.member(LA(5))) && (_tokenSet_9.member(LA(6))) && (_tokenSet_10.member(LA(7))))) {
								int _m30 = mark();
								synPredMatched30 = true;
								inputState.guessing++;
								try {
									{
									match(IDENTIFIER);
									match(DOT);
									match(IDENTIFIER);
									match(OPEN_PAREN);
									}
								}
								catch (RecognitionException pe) {
									synPredMatched30 = false;
								}
								rewind(_m30);
inputState.guessing--;
							}
							if ( synPredMatched30 ) {
								collection_path_expression();
								astFactory.addASTChild(currentAST, returnAST);
								match(LITERAL_as);
								field_alias();
								astFactory.addASTChild(currentAST, returnAST);
							}
							else if ((LA(1)==IDENTIFIER) && (_tokenSet_11.member(LA(2))) && (_tokenSet_12.member(LA(3))) && (_tokenSet_13.member(LA(4))) && (_tokenSet_14.member(LA(5))) && (_tokenSet_15.member(LA(6))) && (_tokenSet_16.member(LA(7)))) {
								thing();
								astFactory.addASTChild(currentAST, returnAST);
								{
								switch ( LA(1)) {
								case LITERAL_as:
								{
									match(LITERAL_as);
									field_alias();
									astFactory.addASTChild(currentAST, returnAST);
									break;
								}
								case EOF:
								case COMMA:
								case LITERAL_from:
								case LITERAL_where:
								case LITERAL_group:
								case LITERAL_order:
								case CLOSE_PAREN:
								case LITERAL_limit:
								{
									break;
								}
								default:
								{
									throw new NoViableAltException(LT(1), getFilename());
								}
								}
								}
							}
							else if ((_tokenSet_17.member(LA(1))) && (LA(2)==LITERAL_as)) {
								constant();
								astFactory.addASTChild(currentAST, returnAST);
								match(LITERAL_as);
								field_alias();
								astFactory.addASTChild(currentAST, returnAST);
							}
							else if ((_tokenSet_18.member(LA(1))) && (LA(2)==OPEN_PAREN) && (_tokenSet_19.member(LA(3))) && (_tokenSet_4.member(LA(4))) && (_tokenSet_20.member(LA(5))) && (_tokenSet_4.member(LA(6))) && (_tokenSet_5.member(LA(7)))) {
								safe_function();
								astFactory.addASTChild(currentAST, returnAST);
								match(LITERAL_as);
								field_alias();
								astFactory.addASTChild(currentAST, returnAST);
							}
							else if ((LA(1)==OPEN_PAREN) && (_tokenSet_0.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_20.member(LA(4))) && (_tokenSet_4.member(LA(5))) && (_tokenSet_5.member(LA(6))) && (_tokenSet_6.member(LA(7)))) {
								paren_value();
								astFactory.addASTChild(currentAST, returnAST);
								match(LITERAL_as);
								field_alias();
								astFactory.addASTChild(currentAST, returnAST);
							}
						else {
							throw new NoViableAltException(LT(1), getFilename());
						}
						}}}
						}
						if ( inputState.guessing==0 ) {
							select_value_AST = (AST)currentAST.root;
							select_value_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(SELECT_VALUE,"SELECT_VALUE")).add(select_value_AST));
							currentAST.root = select_value_AST;
							currentAST.child = select_value_AST!=null &&select_value_AST.getFirstChild()!=null ?
								select_value_AST.getFirstChild() : select_value_AST;
							currentAST.advanceChildToEnd();
						}
						select_value_AST = (AST)currentAST.root;
						returnAST = select_value_AST;
						select_valueCache.put(LT(1), returnAST);
					} catch (RecognitionException e) {
						select_valueCache.put(LT(1), e);
						throw e;
					} catch (TokenStreamException e) {
						select_valueCache.put(LT(1), e);
						throw e;
					}
				}
			}
			
	IdentityHashMap abstract_tableCache = new IdentityHashMap();
	public final void abstract_table() throws RecognitionException, TokenStreamException {
		Object retval = abstract_tableCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST abstract_table_AST = null;
				
				if ((LA(1)==IDENTIFIER)) {
					table();
					astFactory.addASTChild(currentAST, returnAST);
					abstract_table_AST = (AST)currentAST.root;
				}
				else if ((LA(1)==OPEN_PAREN) && (LA(2)==LITERAL_explain||LA(2)==LITERAL_select)) {
					subquery();
					astFactory.addASTChild(currentAST, returnAST);
					abstract_table_AST = (AST)currentAST.root;
				}
				else if ((LA(1)==OPEN_PAREN) && (LA(2)==IDENTIFIER)) {
					multitable();
					astFactory.addASTChild(currentAST, returnAST);
					abstract_table_AST = (AST)currentAST.root;
				}
				else {
					boolean synPredMatched37 = false;
					if (((LA(1)==LITERAL_bag||LA(1)==QUESTION_MARK) && (LA(2)==OPEN_PAREN||LA(2)==COLONTYPE) && (LA(3)==IDENTIFIER||LA(3)==INTEGER) && (LA(4)==LITERAL_as||LA(4)==DOT||LA(4)==CLOSE_PAREN) && (LA(5)==IDENTIFIER||LA(5)==COLONTYPE) && (_tokenSet_21.member(LA(6))) && (_tokenSet_22.member(LA(7))))) {
						int _m37 = mark();
						synPredMatched37 = true;
						inputState.guessing++;
						try {
							{
							query_class_bag();
							}
						}
						catch (RecognitionException pe) {
							synPredMatched37 = false;
						}
						rewind(_m37);
inputState.guessing--;
					}
					if ( synPredMatched37 ) {
						query_class_bag();
						astFactory.addASTChild(currentAST, returnAST);
						abstract_table_AST = (AST)currentAST.root;
					}
					else if ((LA(1)==LITERAL_bag||LA(1)==QUESTION_MARK) && (LA(2)==OPEN_PAREN||LA(2)==COLONTYPE) && (LA(3)==OPEN_PAREN||LA(3)==INTEGER) && (LA(4)==IDENTIFIER||LA(4)==CLOSE_PAREN) && (_tokenSet_23.member(LA(5))) && (LA(6)==LITERAL_as||LA(6)==IDENTIFIER||LA(6)==OPEN_PAREN) && (_tokenSet_24.member(LA(7)))) {
						query_class_bag_multi();
						astFactory.addASTChild(currentAST, returnAST);
						abstract_table_AST = (AST)currentAST.root;
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					returnAST = abstract_table_AST;
					abstract_tableCache.put(LT(1), returnAST);
				} catch (RecognitionException e) {
					abstract_tableCache.put(LT(1), e);
					throw e;
				} catch (TokenStreamException e) {
					abstract_tableCache.put(LT(1), e);
					throw e;
				}
			}
		}
		
	IdentityHashMap abstract_constraintCache = new IdentityHashMap();
	public final void abstract_constraint() throws RecognitionException, TokenStreamException {
		Object retval = abstract_constraintCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST abstract_constraint_AST = null;
				
				boolean synPredMatched118 = false;
				if (((_tokenSet_25.member(LA(1))) && (_tokenSet_26.member(LA(2))) && (_tokenSet_27.member(LA(3))) && (_tokenSet_28.member(LA(4))) && (_tokenSet_29.member(LA(5))) && (_tokenSet_29.member(LA(6))) && (_tokenSet_15.member(LA(7))))) {
					int _m118 = mark();
					synPredMatched118 = true;
					inputState.guessing++;
					try {
						{
						constraint_set();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched118 = false;
					}
					rewind(_m118);
inputState.guessing--;
				}
				if ( synPredMatched118 ) {
					constraint_set();
					astFactory.addASTChild(currentAST, returnAST);
					abstract_constraint_AST = (AST)currentAST.root;
				}
				else if ((_tokenSet_25.member(LA(1))) && (_tokenSet_30.member(LA(2))) && (_tokenSet_31.member(LA(3))) && (_tokenSet_32.member(LA(4))) && (_tokenSet_15.member(LA(5))) && (_tokenSet_15.member(LA(6))) && (_tokenSet_15.member(LA(7)))) {
					safe_abstract_constraint();
					astFactory.addASTChild(currentAST, returnAST);
					abstract_constraint_AST = (AST)currentAST.root;
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				returnAST = abstract_constraint_AST;
				abstract_constraintCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				abstract_constraintCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				abstract_constraintCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap abstract_valueCache = new IdentityHashMap();
	public final void abstract_value() throws RecognitionException, TokenStreamException {
		Object retval = abstract_valueCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST abstract_value_AST = null;
				
				boolean synPredMatched43 = false;
				if (((_tokenSet_0.member(LA(1))) && (_tokenSet_1.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_33.member(LA(4))) && (_tokenSet_34.member(LA(5))) && (_tokenSet_35.member(LA(6))) && (_tokenSet_14.member(LA(7))))) {
					int _m43 = mark();
					synPredMatched43 = true;
					inputState.guessing++;
					try {
						{
						unsafe_function();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched43 = false;
					}
					rewind(_m43);
inputState.guessing--;
				}
				if ( synPredMatched43 ) {
					unsafe_function();
					astFactory.addASTChild(currentAST, returnAST);
					abstract_value_AST = (AST)currentAST.root;
				}
				else {
					boolean synPredMatched45 = false;
					if (((_tokenSet_0.member(LA(1))) && (_tokenSet_7.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_33.member(LA(4))) && (_tokenSet_34.member(LA(5))) && (_tokenSet_35.member(LA(6))) && (_tokenSet_14.member(LA(7))))) {
						int _m45 = mark();
						synPredMatched45 = true;
						inputState.guessing++;
						try {
							{
							typecast();
							}
						}
						catch (RecognitionException pe) {
							synPredMatched45 = false;
						}
						rewind(_m45);
inputState.guessing--;
					}
					if ( synPredMatched45 ) {
						typecast();
						astFactory.addASTChild(currentAST, returnAST);
						abstract_value_AST = (AST)currentAST.root;
					}
					else if ((_tokenSet_17.member(LA(1))) && (_tokenSet_36.member(LA(2)))) {
						constant();
						astFactory.addASTChild(currentAST, returnAST);
						abstract_value_AST = (AST)currentAST.root;
					}
					else if ((LA(1)==IDENTIFIER) && (_tokenSet_37.member(LA(2))) && (_tokenSet_38.member(LA(3))) && (_tokenSet_35.member(LA(4))) && (_tokenSet_14.member(LA(5))) && (_tokenSet_15.member(LA(6))) && (_tokenSet_15.member(LA(7)))) {
						thing();
						astFactory.addASTChild(currentAST, returnAST);
						abstract_value_AST = (AST)currentAST.root;
					}
					else if ((_tokenSet_18.member(LA(1))) && (LA(2)==OPEN_PAREN) && (_tokenSet_19.member(LA(3))) && (_tokenSet_4.member(LA(4))) && (_tokenSet_33.member(LA(5))) && (_tokenSet_34.member(LA(6))) && (_tokenSet_35.member(LA(7)))) {
						safe_function();
						astFactory.addASTChild(currentAST, returnAST);
						abstract_value_AST = (AST)currentAST.root;
					}
					else if ((LA(1)==OPEN_PAREN) && (_tokenSet_0.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_33.member(LA(4))) && (_tokenSet_34.member(LA(5))) && (_tokenSet_35.member(LA(6))) && (_tokenSet_14.member(LA(7)))) {
						paren_value();
						astFactory.addASTChild(currentAST, returnAST);
						abstract_value_AST = (AST)currentAST.root;
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					returnAST = abstract_value_AST;
					abstract_valueCache.put(LT(1), returnAST);
				} catch (RecognitionException e) {
					abstract_valueCache.put(LT(1), e);
					throw e;
				} catch (TokenStreamException e) {
					abstract_valueCache.put(LT(1), e);
					throw e;
				}
			}
		}
		
	IdentityHashMap orderby_valueCache = new IdentityHashMap();
	public final void orderby_value() throws RecognitionException, TokenStreamException {
		Object retval = orderby_valueCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST orderby_value_AST = null;
				
				boolean synPredMatched40 = false;
				if (((_tokenSet_0.member(LA(1))) && (_tokenSet_39.member(LA(2))) && (_tokenSet_40.member(LA(3))) && (_tokenSet_41.member(LA(4))) && (_tokenSet_42.member(LA(5))) && (_tokenSet_43.member(LA(6))) && (_tokenSet_44.member(LA(7))))) {
					int _m40 = mark();
					synPredMatched40 = true;
					inputState.guessing++;
					try {
						{
						abstract_value();
						match(LITERAL_desc);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched40 = false;
					}
					rewind(_m40);
inputState.guessing--;
				}
				if ( synPredMatched40 ) {
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(LITERAL_desc);
					if ( inputState.guessing==0 ) {
						orderby_value_AST = (AST)currentAST.root;
						orderby_value_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(ORDER_DESC,"ORDER_DESC")).add(orderby_value_AST));
						currentAST.root = orderby_value_AST;
						currentAST.child = orderby_value_AST!=null &&orderby_value_AST.getFirstChild()!=null ?
							orderby_value_AST.getFirstChild() : orderby_value_AST;
						currentAST.advanceChildToEnd();
					}
					orderby_value_AST = (AST)currentAST.root;
				}
				else if ((_tokenSet_0.member(LA(1))) && (_tokenSet_40.member(LA(2))) && (_tokenSet_45.member(LA(3))) && (_tokenSet_42.member(LA(4))) && (_tokenSet_46.member(LA(5))) && (_tokenSet_44.member(LA(6))) && (_tokenSet_47.member(LA(7)))) {
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					orderby_value_AST = (AST)currentAST.root;
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				returnAST = orderby_value_AST;
				orderby_valueCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				orderby_valueCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				orderby_valueCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap unsafe_functionCache = new IdentityHashMap();
	public final void unsafe_function() throws RecognitionException, TokenStreamException {
		Object retval = unsafe_functionCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST unsafe_function_AST = null;
				
				{
				boolean synPredMatched104 = false;
				if (((_tokenSet_0.member(LA(1))) && (_tokenSet_48.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_49.member(LA(4))) && (_tokenSet_34.member(LA(5))) && (_tokenSet_35.member(LA(6))) && (_tokenSet_14.member(LA(7))))) {
					int _m104 = mark();
					synPredMatched104 = true;
					inputState.guessing++;
					try {
						{
						safe_abstract_value();
						match(PLUS);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched104 = false;
					}
					rewind(_m104);
inputState.guessing--;
				}
				if ( synPredMatched104 ) {
					safe_abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					AST tmp27_AST = null;
					tmp27_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp27_AST);
					match(PLUS);
					safe_abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					boolean synPredMatched106 = false;
					if (((_tokenSet_0.member(LA(1))) && (_tokenSet_50.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_49.member(LA(4))) && (_tokenSet_34.member(LA(5))) && (_tokenSet_35.member(LA(6))) && (_tokenSet_14.member(LA(7))))) {
						int _m106 = mark();
						synPredMatched106 = true;
						inputState.guessing++;
						try {
							{
							safe_abstract_value();
							match(PERCENT);
							}
						}
						catch (RecognitionException pe) {
							synPredMatched106 = false;
						}
						rewind(_m106);
inputState.guessing--;
					}
					if ( synPredMatched106 ) {
						safe_abstract_value();
						astFactory.addASTChild(currentAST, returnAST);
						AST tmp28_AST = null;
						tmp28_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp28_AST);
						match(PERCENT);
						safe_abstract_value();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						boolean synPredMatched108 = false;
						if (((_tokenSet_0.member(LA(1))) && (_tokenSet_51.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_49.member(LA(4))) && (_tokenSet_34.member(LA(5))) && (_tokenSet_35.member(LA(6))) && (_tokenSet_14.member(LA(7))))) {
							int _m108 = mark();
							synPredMatched108 = true;
							inputState.guessing++;
							try {
								{
								safe_abstract_value();
								match(ASTERISK);
								}
							}
							catch (RecognitionException pe) {
								synPredMatched108 = false;
							}
							rewind(_m108);
inputState.guessing--;
						}
						if ( synPredMatched108 ) {
							safe_abstract_value();
							astFactory.addASTChild(currentAST, returnAST);
							AST tmp29_AST = null;
							tmp29_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp29_AST);
							match(ASTERISK);
							safe_abstract_value();
							astFactory.addASTChild(currentAST, returnAST);
						}
						else {
							boolean synPredMatched110 = false;
							if (((_tokenSet_0.member(LA(1))) && (_tokenSet_52.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_49.member(LA(4))) && (_tokenSet_34.member(LA(5))) && (_tokenSet_35.member(LA(6))) && (_tokenSet_14.member(LA(7))))) {
								int _m110 = mark();
								synPredMatched110 = true;
								inputState.guessing++;
								try {
									{
									safe_abstract_value();
									match(DIVIDE);
									}
								}
								catch (RecognitionException pe) {
									synPredMatched110 = false;
								}
								rewind(_m110);
inputState.guessing--;
							}
							if ( synPredMatched110 ) {
								safe_abstract_value();
								astFactory.addASTChild(currentAST, returnAST);
								AST tmp30_AST = null;
								tmp30_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp30_AST);
								match(DIVIDE);
								safe_abstract_value();
								astFactory.addASTChild(currentAST, returnAST);
							}
							else {
								boolean synPredMatched112 = false;
								if (((_tokenSet_0.member(LA(1))) && (_tokenSet_53.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_49.member(LA(4))) && (_tokenSet_34.member(LA(5))) && (_tokenSet_35.member(LA(6))) && (_tokenSet_14.member(LA(7))))) {
									int _m112 = mark();
									synPredMatched112 = true;
									inputState.guessing++;
									try {
										{
										safe_abstract_value();
										match(POWER);
										}
									}
									catch (RecognitionException pe) {
										synPredMatched112 = false;
									}
									rewind(_m112);
inputState.guessing--;
								}
								if ( synPredMatched112 ) {
									safe_abstract_value();
									astFactory.addASTChild(currentAST, returnAST);
									AST tmp31_AST = null;
									tmp31_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp31_AST);
									match(POWER);
									safe_abstract_value();
									astFactory.addASTChild(currentAST, returnAST);
								}
								else {
									boolean synPredMatched114 = false;
									if (((_tokenSet_0.member(LA(1))) && (_tokenSet_54.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_49.member(LA(4))) && (_tokenSet_34.member(LA(5))) && (_tokenSet_35.member(LA(6))) && (_tokenSet_14.member(LA(7))))) {
										int _m114 = mark();
										synPredMatched114 = true;
										inputState.guessing++;
										try {
											{
											safe_abstract_value();
											match(MINUS);
											}
										}
										catch (RecognitionException pe) {
											synPredMatched114 = false;
										}
										rewind(_m114);
inputState.guessing--;
									}
									if ( synPredMatched114 ) {
										safe_abstract_value();
										astFactory.addASTChild(currentAST, returnAST);
										AST tmp32_AST = null;
										tmp32_AST = astFactory.create(LT(1));
										astFactory.addASTChild(currentAST, tmp32_AST);
										match(MINUS);
										safe_abstract_value();
										astFactory.addASTChild(currentAST, returnAST);
									}
									else {
										throw new NoViableAltException(LT(1), getFilename());
									}
									}}}}}
									}
									if ( inputState.guessing==0 ) {
										unsafe_function_AST = (AST)currentAST.root;
										unsafe_function_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(UNSAFE_FUNCTION,"UNSAFE_FUNCTION")).add(unsafe_function_AST));
										currentAST.root = unsafe_function_AST;
										currentAST.child = unsafe_function_AST!=null &&unsafe_function_AST.getFirstChild()!=null ?
											unsafe_function_AST.getFirstChild() : unsafe_function_AST;
										currentAST.advanceChildToEnd();
									}
									unsafe_function_AST = (AST)currentAST.root;
									returnAST = unsafe_function_AST;
									unsafe_functionCache.put(LT(1), returnAST);
								} catch (RecognitionException e) {
									unsafe_functionCache.put(LT(1), e);
									throw e;
								} catch (TokenStreamException e) {
									unsafe_functionCache.put(LT(1), e);
									throw e;
								}
							}
						}
						
	IdentityHashMap field_aliasCache = new IdentityHashMap();
	public final void field_alias() throws RecognitionException, TokenStreamException {
		Object retval = field_aliasCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST field_alias_AST = null;
				
				AST tmp33_AST = null;
				tmp33_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp33_AST);
				match(IDENTIFIER);
				if ( inputState.guessing==0 ) {
					field_alias_AST = (AST)currentAST.root;
					field_alias_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(FIELD_ALIAS,"FIELD_ALIAS")).add(field_alias_AST));
					currentAST.root = field_alias_AST;
					currentAST.child = field_alias_AST!=null &&field_alias_AST.getFirstChild()!=null ?
						field_alias_AST.getFirstChild() : field_alias_AST;
					currentAST.advanceChildToEnd();
				}
				field_alias_AST = (AST)currentAST.root;
				returnAST = field_alias_AST;
				field_aliasCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				field_aliasCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				field_aliasCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap typecastCache = new IdentityHashMap();
	public final void typecast() throws RecognitionException, TokenStreamException {
		Object retval = typecastCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST typecast_AST = null;
				
				{
				switch ( LA(1)) {
				case INTEGER:
				case QUOTED_STRING:
				case FLOAT:
				case LITERAL_true:
				case LITERAL_false:
				case LITERAL_null:
				{
					constant();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case IDENTIFIER:
				{
					thing();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case LITERAL_count:
				case LITERAL_max:
				case LITERAL_min:
				case LITERAL_sum:
				case LITERAL_avg:
				case LITERAL_substr:
				case LITERAL_indexof:
				case LITERAL_lower:
				case LITERAL_upper:
				case LITERAL_stddev:
				{
					safe_function();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case OPEN_PAREN:
				{
					paren_value();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				match(COLONTYPE);
				AST tmp35_AST = null;
				tmp35_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp35_AST);
				match(IDENTIFIER);
				if ( inputState.guessing==0 ) {
					typecast_AST = (AST)currentAST.root;
					typecast_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TYPECAST,"TYPECAST")).add(typecast_AST));
					currentAST.root = typecast_AST;
					currentAST.child = typecast_AST!=null &&typecast_AST.getFirstChild()!=null ?
						typecast_AST.getFirstChild() : typecast_AST;
					currentAST.advanceChildToEnd();
				}
				typecast_AST = (AST)currentAST.root;
				returnAST = typecast_AST;
				typecastCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				typecastCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				typecastCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap collection_path_expressionCache = new IdentityHashMap();
	public final void collection_path_expression() throws RecognitionException, TokenStreamException {
		Object retval = collection_path_expressionCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST collection_path_expression_AST = null;
				
				AST tmp36_AST = null;
				tmp36_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp36_AST);
				match(IDENTIFIER);
				match(DOT);
				AST tmp38_AST = null;
				tmp38_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp38_AST);
				match(IDENTIFIER);
				match(OPEN_PAREN);
				{
				switch ( LA(1)) {
				case LITERAL_select:
				{
					match(LITERAL_select);
					{
					switch ( LA(1)) {
					case LITERAL_singleton:
					{
						AST tmp41_AST = null;
						tmp41_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp41_AST);
						match(LITERAL_singleton);
						break;
					}
					case IDENTIFIER:
					case OPEN_PAREN:
					case INTEGER:
					case QUOTED_STRING:
					case FLOAT:
					case LITERAL_true:
					case LITERAL_false:
					case LITERAL_null:
					case LITERAL_count:
					case LITERAL_max:
					case LITERAL_min:
					case LITERAL_sum:
					case LITERAL_avg:
					case LITERAL_substr:
					case LITERAL_indexof:
					case LITERAL_lower:
					case LITERAL_upper:
					case LITERAL_stddev:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					collection_select_list();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case LITERAL_from:
				case LITERAL_where:
				case CLOSE_PAREN:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				{
				switch ( LA(1)) {
				case LITERAL_from:
				{
					from_list();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case LITERAL_where:
				case CLOSE_PAREN:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				{
				switch ( LA(1)) {
				case LITERAL_where:
				{
					where_clause();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case CLOSE_PAREN:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				match(CLOSE_PAREN);
				if ( inputState.guessing==0 ) {
					collection_path_expression_AST = (AST)currentAST.root;
					collection_path_expression_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(FIELD,"FIELD")).add(collection_path_expression_AST));
					currentAST.root = collection_path_expression_AST;
					currentAST.child = collection_path_expression_AST!=null &&collection_path_expression_AST.getFirstChild()!=null ?
						collection_path_expression_AST.getFirstChild() : collection_path_expression_AST;
					currentAST.advanceChildToEnd();
				}
				collection_path_expression_AST = (AST)currentAST.root;
				returnAST = collection_path_expression_AST;
				collection_path_expressionCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				collection_path_expressionCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				collection_path_expressionCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap thingCache = new IdentityHashMap();
	public final void thing() throws RecognitionException, TokenStreamException {
		Object retval = thingCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST thing_AST = null;
				
				{
				_loop97:
				do {
					if ((LA(1)==IDENTIFIER) && (LA(2)==DOT)) {
						AST tmp43_AST = null;
						tmp43_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp43_AST);
						match(IDENTIFIER);
						match(DOT);
					}
					else {
						break _loop97;
					}
					
				} while (true);
				}
				AST tmp45_AST = null;
				tmp45_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp45_AST);
				match(IDENTIFIER);
				if ( inputState.guessing==0 ) {
					thing_AST = (AST)currentAST.root;
					thing_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(FIELD,"FIELD")).add(thing_AST));
					currentAST.root = thing_AST;
					currentAST.child = thing_AST!=null &&thing_AST.getFirstChild()!=null ?
						thing_AST.getFirstChild() : thing_AST;
					currentAST.advanceChildToEnd();
				}
				thing_AST = (AST)currentAST.root;
				returnAST = thing_AST;
				thingCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				thingCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				thingCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap constantCache = new IdentityHashMap();
	public final void constant() throws RecognitionException, TokenStreamException {
		Object retval = constantCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST constant_AST = null;
				
				{
				switch ( LA(1)) {
				case QUOTED_STRING:
				{
					AST tmp46_AST = null;
					tmp46_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp46_AST);
					match(QUOTED_STRING);
					break;
				}
				case INTEGER:
				{
					AST tmp47_AST = null;
					tmp47_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp47_AST);
					match(INTEGER);
					break;
				}
				case FLOAT:
				{
					AST tmp48_AST = null;
					tmp48_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp48_AST);
					match(FLOAT);
					break;
				}
				case LITERAL_true:
				{
					AST tmp49_AST = null;
					tmp49_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp49_AST);
					match(LITERAL_true);
					break;
				}
				case LITERAL_false:
				{
					AST tmp50_AST = null;
					tmp50_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp50_AST);
					match(LITERAL_false);
					break;
				}
				case LITERAL_null:
				{
					AST tmp51_AST = null;
					tmp51_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp51_AST);
					match(LITERAL_null);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					constant_AST = (AST)currentAST.root;
					constant_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(CONSTANT,"CONSTANT")).add(constant_AST));
					currentAST.root = constant_AST;
					currentAST.child = constant_AST!=null &&constant_AST.getFirstChild()!=null ?
						constant_AST.getFirstChild() : constant_AST;
					currentAST.advanceChildToEnd();
				}
				constant_AST = (AST)currentAST.root;
				returnAST = constant_AST;
				constantCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				constantCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				constantCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap safe_functionCache = new IdentityHashMap();
	public final void safe_function() throws RecognitionException, TokenStreamException {
		Object retval = safe_functionCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST safe_function_AST = null;
				
				{
				switch ( LA(1)) {
				case LITERAL_count:
				{
					AST tmp52_AST = null;
					tmp52_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp52_AST);
					match(LITERAL_count);
					match(OPEN_PAREN);
					match(ASTERISK);
					match(CLOSE_PAREN);
					break;
				}
				case LITERAL_max:
				{
					AST tmp56_AST = null;
					tmp56_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp56_AST);
					match(LITERAL_max);
					match(OPEN_PAREN);
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(CLOSE_PAREN);
					break;
				}
				case LITERAL_min:
				{
					AST tmp59_AST = null;
					tmp59_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp59_AST);
					match(LITERAL_min);
					match(OPEN_PAREN);
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(CLOSE_PAREN);
					break;
				}
				case LITERAL_sum:
				{
					AST tmp62_AST = null;
					tmp62_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp62_AST);
					match(LITERAL_sum);
					match(OPEN_PAREN);
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(CLOSE_PAREN);
					break;
				}
				case LITERAL_avg:
				{
					AST tmp65_AST = null;
					tmp65_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp65_AST);
					match(LITERAL_avg);
					match(OPEN_PAREN);
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(CLOSE_PAREN);
					break;
				}
				case LITERAL_substr:
				{
					AST tmp68_AST = null;
					tmp68_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp68_AST);
					match(LITERAL_substr);
					match(OPEN_PAREN);
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(COMMA);
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					{
					switch ( LA(1)) {
					case COMMA:
					{
						match(COMMA);
						abstract_value();
						astFactory.addASTChild(currentAST, returnAST);
						break;
					}
					case CLOSE_PAREN:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					match(CLOSE_PAREN);
					break;
				}
				case LITERAL_indexof:
				{
					AST tmp73_AST = null;
					tmp73_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp73_AST);
					match(LITERAL_indexof);
					match(OPEN_PAREN);
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(COMMA);
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(CLOSE_PAREN);
					break;
				}
				case LITERAL_lower:
				{
					AST tmp77_AST = null;
					tmp77_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp77_AST);
					match(LITERAL_lower);
					match(OPEN_PAREN);
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(CLOSE_PAREN);
					break;
				}
				case LITERAL_upper:
				{
					AST tmp80_AST = null;
					tmp80_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp80_AST);
					match(LITERAL_upper);
					match(OPEN_PAREN);
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(CLOSE_PAREN);
					break;
				}
				case LITERAL_stddev:
				{
					AST tmp83_AST = null;
					tmp83_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp83_AST);
					match(LITERAL_stddev);
					match(OPEN_PAREN);
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(CLOSE_PAREN);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					safe_function_AST = (AST)currentAST.root;
					safe_function_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(SAFE_FUNCTION,"SAFE_FUNCTION")).add(safe_function_AST));
					currentAST.root = safe_function_AST;
					currentAST.child = safe_function_AST!=null &&safe_function_AST.getFirstChild()!=null ?
						safe_function_AST.getFirstChild() : safe_function_AST;
					currentAST.advanceChildToEnd();
				}
				safe_function_AST = (AST)currentAST.root;
				returnAST = safe_function_AST;
				safe_functionCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				safe_functionCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				safe_functionCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap paren_valueCache = new IdentityHashMap();
	public final void paren_value() throws RecognitionException, TokenStreamException {
		Object retval = paren_valueCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST paren_value_AST = null;
				
				match(OPEN_PAREN);
				abstract_value();
				astFactory.addASTChild(currentAST, returnAST);
				match(CLOSE_PAREN);
				paren_value_AST = (AST)currentAST.root;
				returnAST = paren_value_AST;
				paren_valueCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				paren_valueCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				paren_valueCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap objectstorebagCache = new IdentityHashMap();
	public final void objectstorebag() throws RecognitionException, TokenStreamException {
		Object retval = objectstorebagCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST objectstorebag_AST = null;
				
				match(LITERAL_bag);
				match(OPEN_PAREN);
				AST tmp90_AST = null;
				tmp90_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp90_AST);
				match(INTEGER);
				match(CLOSE_PAREN);
				if ( inputState.guessing==0 ) {
					objectstorebag_AST = (AST)currentAST.root;
					objectstorebag_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(OBJECTSTOREBAG,"OBJECTSTOREBAG")).add(objectstorebag_AST));
					currentAST.root = objectstorebag_AST;
					currentAST.child = objectstorebag_AST!=null &&objectstorebag_AST.getFirstChild()!=null ?
						objectstorebag_AST.getFirstChild() : objectstorebag_AST;
					currentAST.advanceChildToEnd();
				}
				objectstorebag_AST = (AST)currentAST.root;
				returnAST = objectstorebag_AST;
				objectstorebagCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				objectstorebagCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				objectstorebagCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap bags_forCache = new IdentityHashMap();
	public final void bags_for() throws RecognitionException, TokenStreamException {
		Object retval = bags_forCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST bags_for_AST = null;
				
				match(LITERAL_bags);
				match(LITERAL_for);
				AST tmp94_AST = null;
				tmp94_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp94_AST);
				match(INTEGER);
				{
				switch ( LA(1)) {
				case LITERAL_in:
				{
					match(LITERAL_in);
					match(LITERAL_bags);
					AST tmp97_AST = null;
					tmp97_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp97_AST);
					match(QUESTION_MARK);
					break;
				}
				case EOF:
				case COMMA:
				case LITERAL_from:
				case LITERAL_where:
				case LITERAL_group:
				case LITERAL_order:
				case CLOSE_PAREN:
				case LITERAL_limit:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					bags_for_AST = (AST)currentAST.root;
					bags_for_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(BAGS_FOR,"BAGS_FOR")).add(bags_for_AST));
					currentAST.root = bags_for_AST;
					currentAST.child = bags_for_AST!=null &&bags_for_AST.getFirstChild()!=null ?
						bags_for_AST.getFirstChild() : bags_for_AST;
					currentAST.advanceChildToEnd();
				}
				bags_for_AST = (AST)currentAST.root;
				returnAST = bags_for_AST;
				bags_forCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				bags_forCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				bags_forCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap tableCache = new IdentityHashMap();
	public final void table() throws RecognitionException, TokenStreamException {
		Object retval = tableCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST table_AST = null;
				
				table_name();
				astFactory.addASTChild(currentAST, returnAST);
				{
				switch ( LA(1)) {
				case LITERAL_as:
				case IDENTIFIER:
				{
					{
					switch ( LA(1)) {
					case LITERAL_as:
					{
						match(LITERAL_as);
						break;
					}
					case IDENTIFIER:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					table_alias();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case EOF:
				case COMMA:
				case LITERAL_where:
				case LITERAL_group:
				case LITERAL_order:
				case CLOSE_PAREN:
				case LITERAL_limit:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					table_AST = (AST)currentAST.root;
					table_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TABLE,"TABLE")).add(table_AST));
					currentAST.root = table_AST;
					currentAST.child = table_AST!=null &&table_AST.getFirstChild()!=null ?
						table_AST.getFirstChild() : table_AST;
					currentAST.advanceChildToEnd();
				}
				table_AST = (AST)currentAST.root;
				returnAST = table_AST;
				tableCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				tableCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				tableCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap subqueryCache = new IdentityHashMap();
	public final void subquery() throws RecognitionException, TokenStreamException {
		Object retval = subqueryCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST subquery_AST = null;
				
				match(OPEN_PAREN);
				iql_statement();
				astFactory.addASTChild(currentAST, returnAST);
				{
				switch ( LA(1)) {
				case LITERAL_limit:
				{
					subquery_limit();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case CLOSE_PAREN:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				match(CLOSE_PAREN);
				{
				switch ( LA(1)) {
				case LITERAL_as:
				{
					match(LITERAL_as);
					break;
				}
				case IDENTIFIER:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				table_alias();
				astFactory.addASTChild(currentAST, returnAST);
				if ( inputState.guessing==0 ) {
					subquery_AST = (AST)currentAST.root;
					subquery_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(SUBQUERY,"SUBQUERY")).add(subquery_AST));
					currentAST.root = subquery_AST;
					currentAST.child = subquery_AST!=null &&subquery_AST.getFirstChild()!=null ?
						subquery_AST.getFirstChild() : subquery_AST;
					currentAST.advanceChildToEnd();
				}
				subquery_AST = (AST)currentAST.root;
				returnAST = subquery_AST;
				subqueryCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				subqueryCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				subqueryCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap multitableCache = new IdentityHashMap();
	public final void multitable() throws RecognitionException, TokenStreamException {
		Object retval = multitableCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST multitable_AST = null;
				
				match(OPEN_PAREN);
				table_name();
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop62:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						table_name();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop62;
					}
					
				} while (true);
				}
				match(CLOSE_PAREN);
				{
				switch ( LA(1)) {
				case LITERAL_as:
				{
					match(LITERAL_as);
					break;
				}
				case IDENTIFIER:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				table_alias();
				astFactory.addASTChild(currentAST, returnAST);
				if ( inputState.guessing==0 ) {
					multitable_AST = (AST)currentAST.root;
					multitable_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TABLE,"TABLE")).add(multitable_AST));
					currentAST.root = multitable_AST;
					currentAST.child = multitable_AST!=null &&multitable_AST.getFirstChild()!=null ?
						multitable_AST.getFirstChild() : multitable_AST;
					currentAST.advanceChildToEnd();
				}
				multitable_AST = (AST)currentAST.root;
				returnAST = multitable_AST;
				multitableCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				multitableCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				multitableCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap query_class_bagCache = new IdentityHashMap();
	public final void query_class_bag() throws RecognitionException, TokenStreamException {
		Object retval = query_class_bagCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST query_class_bag_AST = null;
				
				switch ( LA(1)) {
				case QUESTION_MARK:
				{
					AST tmp106_AST = null;
					tmp106_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp106_AST);
					match(QUESTION_MARK);
					match(COLONTYPE);
					table_name();
					astFactory.addASTChild(currentAST, returnAST);
					match(LITERAL_as);
					table_alias();
					astFactory.addASTChild(currentAST, returnAST);
					if ( inputState.guessing==0 ) {
						query_class_bag_AST = (AST)currentAST.root;
						query_class_bag_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TABLE,"TABLE")).add(query_class_bag_AST));
						currentAST.root = query_class_bag_AST;
						currentAST.child = query_class_bag_AST!=null &&query_class_bag_AST.getFirstChild()!=null ?
							query_class_bag_AST.getFirstChild() : query_class_bag_AST;
						currentAST.advanceChildToEnd();
					}
					query_class_bag_AST = (AST)currentAST.root;
					break;
				}
				case LITERAL_bag:
				{
					objectstorebag();
					astFactory.addASTChild(currentAST, returnAST);
					match(COLONTYPE);
					table_name();
					astFactory.addASTChild(currentAST, returnAST);
					match(LITERAL_as);
					table_alias();
					astFactory.addASTChild(currentAST, returnAST);
					if ( inputState.guessing==0 ) {
						query_class_bag_AST = (AST)currentAST.root;
						query_class_bag_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TABLE,"TABLE")).add(query_class_bag_AST));
						currentAST.root = query_class_bag_AST;
						currentAST.child = query_class_bag_AST!=null &&query_class_bag_AST.getFirstChild()!=null ?
							query_class_bag_AST.getFirstChild() : query_class_bag_AST;
						currentAST.advanceChildToEnd();
					}
					query_class_bag_AST = (AST)currentAST.root;
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				returnAST = query_class_bag_AST;
				query_class_bagCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				query_class_bagCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				query_class_bagCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap query_class_bag_multiCache = new IdentityHashMap();
	public final void query_class_bag_multi() throws RecognitionException, TokenStreamException {
		Object retval = query_class_bag_multiCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST query_class_bag_multi_AST = null;
				
				switch ( LA(1)) {
				case QUESTION_MARK:
				{
					AST tmp111_AST = null;
					tmp111_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp111_AST);
					match(QUESTION_MARK);
					match(COLONTYPE);
					match(OPEN_PAREN);
					table_name();
					astFactory.addASTChild(currentAST, returnAST);
					{
					_loop70:
					do {
						if ((LA(1)==COMMA)) {
							match(COMMA);
							table_name();
							astFactory.addASTChild(currentAST, returnAST);
						}
						else {
							break _loop70;
						}
						
					} while (true);
					}
					match(CLOSE_PAREN);
					match(LITERAL_as);
					table_alias();
					astFactory.addASTChild(currentAST, returnAST);
					if ( inputState.guessing==0 ) {
						query_class_bag_multi_AST = (AST)currentAST.root;
						query_class_bag_multi_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TABLE,"TABLE")).add(query_class_bag_multi_AST));
						currentAST.root = query_class_bag_multi_AST;
						currentAST.child = query_class_bag_multi_AST!=null &&query_class_bag_multi_AST.getFirstChild()!=null ?
							query_class_bag_multi_AST.getFirstChild() : query_class_bag_multi_AST;
						currentAST.advanceChildToEnd();
					}
					query_class_bag_multi_AST = (AST)currentAST.root;
					break;
				}
				case LITERAL_bag:
				{
					objectstorebag();
					astFactory.addASTChild(currentAST, returnAST);
					match(COLONTYPE);
					match(OPEN_PAREN);
					table_name();
					astFactory.addASTChild(currentAST, returnAST);
					{
					_loop72:
					do {
						if ((LA(1)==COMMA)) {
							match(COMMA);
							table_name();
							astFactory.addASTChild(currentAST, returnAST);
						}
						else {
							break _loop72;
						}
						
					} while (true);
					}
					match(CLOSE_PAREN);
					match(LITERAL_as);
					table_alias();
					astFactory.addASTChild(currentAST, returnAST);
					if ( inputState.guessing==0 ) {
						query_class_bag_multi_AST = (AST)currentAST.root;
						query_class_bag_multi_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TABLE,"TABLE")).add(query_class_bag_multi_AST));
						currentAST.root = query_class_bag_multi_AST;
						currentAST.child = query_class_bag_multi_AST!=null &&query_class_bag_multi_AST.getFirstChild()!=null ?
							query_class_bag_multi_AST.getFirstChild() : query_class_bag_multi_AST;
						currentAST.advanceChildToEnd();
					}
					query_class_bag_multi_AST = (AST)currentAST.root;
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				returnAST = query_class_bag_multi_AST;
				query_class_bag_multiCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				query_class_bag_multiCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				query_class_bag_multiCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap safe_abstract_valueCache = new IdentityHashMap();
	public final void safe_abstract_value() throws RecognitionException, TokenStreamException {
		Object retval = safe_abstract_valueCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST safe_abstract_value_AST = null;
				
				boolean synPredMatched48 = false;
				if (((_tokenSet_0.member(LA(1))) && (_tokenSet_7.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_49.member(LA(4))) && (_tokenSet_34.member(LA(5))) && (_tokenSet_35.member(LA(6))) && (_tokenSet_14.member(LA(7))))) {
					int _m48 = mark();
					synPredMatched48 = true;
					inputState.guessing++;
					try {
						{
						typecast();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched48 = false;
					}
					rewind(_m48);
inputState.guessing--;
				}
				if ( synPredMatched48 ) {
					typecast();
					astFactory.addASTChild(currentAST, returnAST);
					safe_abstract_value_AST = (AST)currentAST.root;
				}
				else if ((_tokenSet_17.member(LA(1))) && (_tokenSet_55.member(LA(2)))) {
					constant();
					astFactory.addASTChild(currentAST, returnAST);
					safe_abstract_value_AST = (AST)currentAST.root;
				}
				else if ((LA(1)==IDENTIFIER) && (_tokenSet_56.member(LA(2))) && (_tokenSet_38.member(LA(3))) && (_tokenSet_35.member(LA(4))) && (_tokenSet_14.member(LA(5))) && (_tokenSet_15.member(LA(6))) && (_tokenSet_15.member(LA(7)))) {
					thing();
					astFactory.addASTChild(currentAST, returnAST);
					safe_abstract_value_AST = (AST)currentAST.root;
				}
				else if ((_tokenSet_18.member(LA(1))) && (LA(2)==OPEN_PAREN) && (_tokenSet_19.member(LA(3))) && (_tokenSet_4.member(LA(4))) && (_tokenSet_49.member(LA(5))) && (_tokenSet_34.member(LA(6))) && (_tokenSet_35.member(LA(7)))) {
					safe_function();
					astFactory.addASTChild(currentAST, returnAST);
					safe_abstract_value_AST = (AST)currentAST.root;
				}
				else if ((LA(1)==OPEN_PAREN) && (_tokenSet_0.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_49.member(LA(4))) && (_tokenSet_34.member(LA(5))) && (_tokenSet_35.member(LA(6))) && (_tokenSet_14.member(LA(7)))) {
					paren_value();
					astFactory.addASTChild(currentAST, returnAST);
					safe_abstract_value_AST = (AST)currentAST.root;
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				returnAST = safe_abstract_value_AST;
				safe_abstract_valueCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				safe_abstract_valueCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				safe_abstract_valueCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap table_aliasCache = new IdentityHashMap();
	public final void table_alias() throws RecognitionException, TokenStreamException {
		Object retval = table_aliasCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST table_alias_AST = null;
				
				AST tmp122_AST = null;
				tmp122_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp122_AST);
				match(IDENTIFIER);
				if ( inputState.guessing==0 ) {
					table_alias_AST = (AST)currentAST.root;
					table_alias_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TABLE_ALIAS,"TABLE_ALIAS")).add(table_alias_AST));
					currentAST.root = table_alias_AST;
					currentAST.child = table_alias_AST!=null &&table_alias_AST.getFirstChild()!=null ?
						table_alias_AST.getFirstChild() : table_alias_AST;
					currentAST.advanceChildToEnd();
				}
				table_alias_AST = (AST)currentAST.root;
				returnAST = table_alias_AST;
				table_aliasCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				table_aliasCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				table_aliasCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap table_nameCache = new IdentityHashMap();
	public final void table_name() throws RecognitionException, TokenStreamException {
		Object retval = table_nameCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST table_name_AST = null;
				
				{
				_loop66:
				do {
					if ((LA(1)==IDENTIFIER) && (LA(2)==DOT)) {
						AST tmp123_AST = null;
						tmp123_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp123_AST);
						match(IDENTIFIER);
						match(DOT);
					}
					else {
						break _loop66;
					}
					
				} while (true);
				}
				AST tmp125_AST = null;
				tmp125_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp125_AST);
				match(IDENTIFIER);
				if ( inputState.guessing==0 ) {
					table_name_AST = (AST)currentAST.root;
					table_name_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TABLE_NAME,"TABLE_NAME")).add(table_name_AST));
					currentAST.root = table_name_AST;
					currentAST.child = table_name_AST!=null &&table_name_AST.getFirstChild()!=null ?
						table_name_AST.getFirstChild() : table_name_AST;
					currentAST.advanceChildToEnd();
				}
				table_name_AST = (AST)currentAST.root;
				returnAST = table_name_AST;
				table_nameCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				table_nameCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				table_nameCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap subquery_limitCache = new IdentityHashMap();
	public final void subquery_limit() throws RecognitionException, TokenStreamException {
		Object retval = subquery_limitCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST subquery_limit_AST = null;
				
				match(LITERAL_limit);
				AST tmp127_AST = null;
				tmp127_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp127_AST);
				match(INTEGER);
				if ( inputState.guessing==0 ) {
					subquery_limit_AST = (AST)currentAST.root;
					subquery_limit_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(SUBQUERY_LIMIT,"SUBQUERY_LIMIT")).add(subquery_limit_AST));
					currentAST.root = subquery_limit_AST;
					currentAST.child = subquery_limit_AST!=null &&subquery_limit_AST.getFirstChild()!=null ?
						subquery_limit_AST.getFirstChild() : subquery_limit_AST;
					currentAST.advanceChildToEnd();
				}
				subquery_limit_AST = (AST)currentAST.root;
				returnAST = subquery_limit_AST;
				subquery_limitCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				subquery_limitCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				subquery_limitCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap collection_select_listCache = new IdentityHashMap();
	public final void collection_select_list() throws RecognitionException, TokenStreamException {
		Object retval = collection_select_listCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST collection_select_list_AST = null;
				
				collection_select_value();
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop86:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						collection_select_value();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop86;
					}
					
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					collection_select_list_AST = (AST)currentAST.root;
					collection_select_list_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(COLLECTION_SELECT_LIST,"COLLECTION_SELECT_LIST")).add(collection_select_list_AST));
					currentAST.root = collection_select_list_AST;
					currentAST.child = collection_select_list_AST!=null &&collection_select_list_AST.getFirstChild()!=null ?
						collection_select_list_AST.getFirstChild() : collection_select_list_AST;
					currentAST.advanceChildToEnd();
				}
				collection_select_list_AST = (AST)currentAST.root;
				returnAST = collection_select_list_AST;
				collection_select_listCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				collection_select_listCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				collection_select_listCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap collection_select_valueCache = new IdentityHashMap();
	public final void collection_select_value() throws RecognitionException, TokenStreamException {
		Object retval = collection_select_valueCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST collection_select_value_AST = null;
				
				{
				boolean synPredMatched90 = false;
				if (((_tokenSet_0.member(LA(1))) && (_tokenSet_1.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_57.member(LA(4))) && (_tokenSet_58.member(LA(5))) && (_tokenSet_59.member(LA(6))) && (_tokenSet_60.member(LA(7))))) {
					int _m90 = mark();
					synPredMatched90 = true;
					inputState.guessing++;
					try {
						{
						unsafe_function();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched90 = false;
					}
					rewind(_m90);
inputState.guessing--;
				}
				if ( synPredMatched90 ) {
					unsafe_function();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					boolean synPredMatched92 = false;
					if (((_tokenSet_0.member(LA(1))) && (_tokenSet_7.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_57.member(LA(4))) && (_tokenSet_58.member(LA(5))) && (_tokenSet_59.member(LA(6))) && (_tokenSet_60.member(LA(7))))) {
						int _m92 = mark();
						synPredMatched92 = true;
						inputState.guessing++;
						try {
							{
							typecast();
							}
						}
						catch (RecognitionException pe) {
							synPredMatched92 = false;
						}
						rewind(_m92);
inputState.guessing--;
					}
					if ( synPredMatched92 ) {
						typecast();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						boolean synPredMatched94 = false;
						if (((LA(1)==IDENTIFIER) && (LA(2)==DOT) && (LA(3)==IDENTIFIER) && (LA(4)==OPEN_PAREN) && (_tokenSet_8.member(LA(5))) && (_tokenSet_61.member(LA(6))) && (_tokenSet_59.member(LA(7))))) {
							int _m94 = mark();
							synPredMatched94 = true;
							inputState.guessing++;
							try {
								{
								collection_path_expression();
								}
							}
							catch (RecognitionException pe) {
								synPredMatched94 = false;
							}
							rewind(_m94);
inputState.guessing--;
						}
						if ( synPredMatched94 ) {
							collection_path_expression();
							astFactory.addASTChild(currentAST, returnAST);
						}
						else if ((LA(1)==IDENTIFIER) && (_tokenSet_62.member(LA(2))) && (_tokenSet_63.member(LA(3))) && (_tokenSet_59.member(LA(4))) && (_tokenSet_60.member(LA(5))) && (_tokenSet_29.member(LA(6))) && (_tokenSet_29.member(LA(7)))) {
							thing();
							astFactory.addASTChild(currentAST, returnAST);
						}
						else if ((_tokenSet_17.member(LA(1))) && (_tokenSet_64.member(LA(2)))) {
							constant();
							astFactory.addASTChild(currentAST, returnAST);
						}
						else if ((_tokenSet_18.member(LA(1))) && (LA(2)==OPEN_PAREN) && (_tokenSet_19.member(LA(3))) && (_tokenSet_4.member(LA(4))) && (_tokenSet_57.member(LA(5))) && (_tokenSet_58.member(LA(6))) && (_tokenSet_59.member(LA(7)))) {
							safe_function();
							astFactory.addASTChild(currentAST, returnAST);
						}
						else if ((LA(1)==OPEN_PAREN) && (_tokenSet_0.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_57.member(LA(4))) && (_tokenSet_58.member(LA(5))) && (_tokenSet_59.member(LA(6))) && (_tokenSet_60.member(LA(7)))) {
							paren_value();
							astFactory.addASTChild(currentAST, returnAST);
						}
						else {
							throw new NoViableAltException(LT(1), getFilename());
						}
						}}
						}
						collection_select_value_AST = (AST)currentAST.root;
						returnAST = collection_select_value_AST;
						collection_select_valueCache.put(LT(1), returnAST);
					} catch (RecognitionException e) {
						collection_select_valueCache.put(LT(1), e);
						throw e;
					} catch (TokenStreamException e) {
						collection_select_valueCache.put(LT(1), e);
						throw e;
					}
				}
			}
			
	IdentityHashMap field_nameCache = new IdentityHashMap();
	public final void field_name() throws RecognitionException, TokenStreamException {
		Object retval = field_nameCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST field_name_AST = null;
				
				AST tmp129_AST = null;
				tmp129_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp129_AST);
				match(IDENTIFIER);
				if ( inputState.guessing==0 ) {
					field_name_AST = (AST)currentAST.root;
					field_name_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(FIELD_NAME,"FIELD_NAME")).add(field_name_AST));
					currentAST.root = field_name_AST;
					currentAST.child = field_name_AST!=null &&field_name_AST.getFirstChild()!=null ?
						field_name_AST.getFirstChild() : field_name_AST;
					currentAST.advanceChildToEnd();
				}
				field_name_AST = (AST)currentAST.root;
				returnAST = field_name_AST;
				field_nameCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				field_nameCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				field_nameCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap constraint_setCache = new IdentityHashMap();
	public final void constraint_set() throws RecognitionException, TokenStreamException {
		Object retval = constraint_setCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST constraint_set_AST = null;
				
				boolean synPredMatched141 = false;
				if (((_tokenSet_25.member(LA(1))) && (_tokenSet_65.member(LA(2))) && (_tokenSet_27.member(LA(3))) && (_tokenSet_28.member(LA(4))) && (_tokenSet_29.member(LA(5))) && (_tokenSet_29.member(LA(6))) && (_tokenSet_15.member(LA(7))))) {
					int _m141 = mark();
					synPredMatched141 = true;
					inputState.guessing++;
					try {
						{
						or_constraint_set();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched141 = false;
					}
					rewind(_m141);
inputState.guessing--;
				}
				if ( synPredMatched141 ) {
					or_constraint_set();
					astFactory.addASTChild(currentAST, returnAST);
					constraint_set_AST = (AST)currentAST.root;
				}
				else if ((_tokenSet_25.member(LA(1))) && (_tokenSet_66.member(LA(2))) && (_tokenSet_27.member(LA(3))) && (_tokenSet_28.member(LA(4))) && (_tokenSet_29.member(LA(5))) && (_tokenSet_29.member(LA(6))) && (_tokenSet_15.member(LA(7)))) {
					and_constraint_set();
					astFactory.addASTChild(currentAST, returnAST);
					constraint_set_AST = (AST)currentAST.root;
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				returnAST = constraint_set_AST;
				constraint_setCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				constraint_setCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				constraint_setCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap safe_abstract_constraintCache = new IdentityHashMap();
	public final void safe_abstract_constraint() throws RecognitionException, TokenStreamException {
		Object retval = safe_abstract_constraintCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST safe_abstract_constraint_AST = null;
				
				switch ( LA(1)) {
				case LITERAL_exists:
				case LITERAL_does:
				{
					subquery_exists_constraint();
					astFactory.addASTChild(currentAST, returnAST);
					safe_abstract_constraint_AST = (AST)currentAST.root;
					break;
				}
				case LITERAL_not:
				{
					not_constraint();
					astFactory.addASTChild(currentAST, returnAST);
					safe_abstract_constraint_AST = (AST)currentAST.root;
					break;
				}
				default:
					boolean synPredMatched121 = false;
					if (((LA(1)==OPEN_PAREN) && (_tokenSet_25.member(LA(2))) && (_tokenSet_67.member(LA(3))) && (_tokenSet_68.member(LA(4))) && (_tokenSet_32.member(LA(5))) && (_tokenSet_29.member(LA(6))) && (_tokenSet_15.member(LA(7))))) {
						int _m121 = mark();
						synPredMatched121 = true;
						inputState.guessing++;
						try {
							{
							paren_constraint();
							}
						}
						catch (RecognitionException pe) {
							synPredMatched121 = false;
						}
						rewind(_m121);
inputState.guessing--;
					}
					if ( synPredMatched121 ) {
						paren_constraint();
						astFactory.addASTChild(currentAST, returnAST);
						safe_abstract_constraint_AST = (AST)currentAST.root;
					}
					else {
						boolean synPredMatched123 = false;
						if (((_tokenSet_0.member(LA(1))) && (_tokenSet_69.member(LA(2))) && (_tokenSet_70.member(LA(3))) && (_tokenSet_71.member(LA(4))) && (_tokenSet_72.member(LA(5))) && (_tokenSet_46.member(LA(6))) && (_tokenSet_44.member(LA(7))))) {
							int _m123 = mark();
							synPredMatched123 = true;
							inputState.guessing++;
							try {
								{
								bag_constraint();
								}
							}
							catch (RecognitionException pe) {
								synPredMatched123 = false;
							}
							rewind(_m123);
inputState.guessing--;
						}
						if ( synPredMatched123 ) {
							bag_constraint();
							astFactory.addASTChild(currentAST, returnAST);
							safe_abstract_constraint_AST = (AST)currentAST.root;
						}
						else {
							boolean synPredMatched125 = false;
							if (((_tokenSet_0.member(LA(1))) && (_tokenSet_69.member(LA(2))) && (_tokenSet_73.member(LA(3))) && (_tokenSet_74.member(LA(4))) && (_tokenSet_75.member(LA(5))) && (_tokenSet_76.member(LA(6))) && (_tokenSet_77.member(LA(7))))) {
								int _m125 = mark();
								synPredMatched125 = true;
								inputState.guessing++;
								try {
									{
									subquery_constraint();
									}
								}
								catch (RecognitionException pe) {
									synPredMatched125 = false;
								}
								rewind(_m125);
inputState.guessing--;
							}
							if ( synPredMatched125 ) {
								subquery_constraint();
								astFactory.addASTChild(currentAST, returnAST);
								safe_abstract_constraint_AST = (AST)currentAST.root;
							}
							else {
								boolean synPredMatched127 = false;
								if (((LA(1)==IDENTIFIER||LA(1)==QUESTION_MARK) && (LA(2)==DOT||LA(2)==LITERAL_does||LA(2)==LITERAL_contains) && (LA(3)==IDENTIFIER||LA(3)==QUESTION_MARK||LA(3)==LITERAL_not) && (_tokenSet_78.member(LA(4))) && (_tokenSet_79.member(LA(5))) && (_tokenSet_80.member(LA(6))) && (_tokenSet_44.member(LA(7))))) {
									int _m127 = mark();
									synPredMatched127 = true;
									inputState.guessing++;
									try {
										{
										contains_constraint();
										}
									}
									catch (RecognitionException pe) {
										synPredMatched127 = false;
									}
									rewind(_m127);
inputState.guessing--;
								}
								if ( synPredMatched127 ) {
									contains_constraint();
									astFactory.addASTChild(currentAST, returnAST);
									safe_abstract_constraint_AST = (AST)currentAST.root;
								}
								else if ((LA(1)==LITERAL_true) && (_tokenSet_81.member(LA(2)))) {
									AST tmp130_AST = null;
									tmp130_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp130_AST);
									match(LITERAL_true);
									safe_abstract_constraint_AST = (AST)currentAST.root;
								}
								else if ((LA(1)==LITERAL_false) && (_tokenSet_81.member(LA(2)))) {
									AST tmp131_AST = null;
									tmp131_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp131_AST);
									match(LITERAL_false);
									safe_abstract_constraint_AST = (AST)currentAST.root;
								}
								else if ((_tokenSet_0.member(LA(1))) && (_tokenSet_82.member(LA(2))) && (_tokenSet_83.member(LA(3))) && (_tokenSet_84.member(LA(4))) && (_tokenSet_46.member(LA(5))) && (_tokenSet_44.member(LA(6))) && (_tokenSet_47.member(LA(7)))) {
									constraint();
									astFactory.addASTChild(currentAST, returnAST);
									safe_abstract_constraint_AST = (AST)currentAST.root;
								}
							else {
								throw new NoViableAltException(LT(1), getFilename());
							}
							}}}}
							returnAST = safe_abstract_constraint_AST;
							safe_abstract_constraintCache.put(LT(1), returnAST);
						} catch (RecognitionException e) {
							safe_abstract_constraintCache.put(LT(1), e);
							throw e;
						} catch (TokenStreamException e) {
							safe_abstract_constraintCache.put(LT(1), e);
							throw e;
						}
					}
				}
				
	IdentityHashMap paren_constraintCache = new IdentityHashMap();
	public final void paren_constraint() throws RecognitionException, TokenStreamException {
		Object retval = paren_constraintCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST paren_constraint_AST = null;
				
				match(OPEN_PAREN);
				abstract_constraint();
				astFactory.addASTChild(currentAST, returnAST);
				match(CLOSE_PAREN);
				paren_constraint_AST = (AST)currentAST.root;
				returnAST = paren_constraint_AST;
				paren_constraintCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				paren_constraintCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				paren_constraintCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap bag_constraintCache = new IdentityHashMap();
	public final void bag_constraint() throws RecognitionException, TokenStreamException {
		Object retval = bag_constraintCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST bag_constraint_AST = null;
				
				boolean synPredMatched154 = false;
				if (((_tokenSet_0.member(LA(1))) && (_tokenSet_85.member(LA(2))) && (_tokenSet_86.member(LA(3))) && (_tokenSet_87.member(LA(4))) && (_tokenSet_88.member(LA(5))) && (_tokenSet_88.member(LA(6))) && (_tokenSet_89.member(LA(7))))) {
					int _m154 = mark();
					synPredMatched154 = true;
					inputState.guessing++;
					try {
						{
						abstract_value();
						match(LITERAL_in);
						objectstorebag();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched154 = false;
					}
					rewind(_m154);
inputState.guessing--;
				}
				if ( synPredMatched154 ) {
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(LITERAL_in);
					objectstorebag();
					astFactory.addASTChild(currentAST, returnAST);
					if ( inputState.guessing==0 ) {
						bag_constraint_AST = (AST)currentAST.root;
						bag_constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(BAG_CONSTRAINT,"BAG_CONSTRAINT")).add(bag_constraint_AST));
						currentAST.root = bag_constraint_AST;
						currentAST.child = bag_constraint_AST!=null &&bag_constraint_AST.getFirstChild()!=null ?
							bag_constraint_AST.getFirstChild() : bag_constraint_AST;
						currentAST.advanceChildToEnd();
					}
					bag_constraint_AST = (AST)currentAST.root;
				}
				else {
					boolean synPredMatched156 = false;
					if (((_tokenSet_0.member(LA(1))) && (_tokenSet_90.member(LA(2))) && (_tokenSet_73.member(LA(3))) && (_tokenSet_91.member(LA(4))) && (_tokenSet_92.member(LA(5))) && (_tokenSet_93.member(LA(6))) && (_tokenSet_93.member(LA(7))))) {
						int _m156 = mark();
						synPredMatched156 = true;
						inputState.guessing++;
						try {
							{
							abstract_value();
							match(LITERAL_not);
							match(LITERAL_in);
							objectstorebag();
							}
						}
						catch (RecognitionException pe) {
							synPredMatched156 = false;
						}
						rewind(_m156);
inputState.guessing--;
					}
					if ( synPredMatched156 ) {
						abstract_value();
						astFactory.addASTChild(currentAST, returnAST);
						match(LITERAL_not);
						match(LITERAL_in);
						objectstorebag();
						astFactory.addASTChild(currentAST, returnAST);
						if ( inputState.guessing==0 ) {
							bag_constraint_AST = (AST)currentAST.root;
							bag_constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(NOT_CONSTRAINT,"NOT_CONSTRAINT")).add((AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(BAG_CONSTRAINT,"BAG_CONSTRAINT")).add(bag_constraint_AST))));
							currentAST.root = bag_constraint_AST;
							currentAST.child = bag_constraint_AST!=null &&bag_constraint_AST.getFirstChild()!=null ?
								bag_constraint_AST.getFirstChild() : bag_constraint_AST;
							currentAST.advanceChildToEnd();
						}
						bag_constraint_AST = (AST)currentAST.root;
					}
					else {
						boolean synPredMatched158 = false;
						if (((_tokenSet_0.member(LA(1))) && (_tokenSet_85.member(LA(2))) && (_tokenSet_94.member(LA(3))) && (_tokenSet_95.member(LA(4))) && (_tokenSet_96.member(LA(5))) && (_tokenSet_46.member(LA(6))) && (_tokenSet_44.member(LA(7))))) {
							int _m158 = mark();
							synPredMatched158 = true;
							inputState.guessing++;
							try {
								{
								abstract_value();
								match(LITERAL_in);
								}
							}
							catch (RecognitionException pe) {
								synPredMatched158 = false;
							}
							rewind(_m158);
inputState.guessing--;
						}
						if ( synPredMatched158 ) {
							abstract_value();
							astFactory.addASTChild(currentAST, returnAST);
							match(LITERAL_in);
							match(QUESTION_MARK);
							if ( inputState.guessing==0 ) {
								bag_constraint_AST = (AST)currentAST.root;
								bag_constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(BAG_CONSTRAINT,"BAG_CONSTRAINT")).add(bag_constraint_AST));
								currentAST.root = bag_constraint_AST;
								currentAST.child = bag_constraint_AST!=null &&bag_constraint_AST.getFirstChild()!=null ?
									bag_constraint_AST.getFirstChild() : bag_constraint_AST;
								currentAST.advanceChildToEnd();
							}
							bag_constraint_AST = (AST)currentAST.root;
						}
						else if ((_tokenSet_0.member(LA(1))) && (_tokenSet_90.member(LA(2))) && (_tokenSet_73.member(LA(3))) && (_tokenSet_97.member(LA(4))) && (_tokenSet_98.member(LA(5))) && (_tokenSet_96.member(LA(6))) && (_tokenSet_46.member(LA(7)))) {
							abstract_value();
							astFactory.addASTChild(currentAST, returnAST);
							match(LITERAL_not);
							match(LITERAL_in);
							match(QUESTION_MARK);
							if ( inputState.guessing==0 ) {
								bag_constraint_AST = (AST)currentAST.root;
								bag_constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(NOT_CONSTRAINT,"NOT_CONSTRAINT")).add((AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(BAG_CONSTRAINT,"BAG_CONSTRAINT")).add(bag_constraint_AST))));
								currentAST.root = bag_constraint_AST;
								currentAST.child = bag_constraint_AST!=null &&bag_constraint_AST.getFirstChild()!=null ?
									bag_constraint_AST.getFirstChild() : bag_constraint_AST;
								currentAST.advanceChildToEnd();
							}
							bag_constraint_AST = (AST)currentAST.root;
						}
						else {
							throw new NoViableAltException(LT(1), getFilename());
						}
						}}
						returnAST = bag_constraint_AST;
						bag_constraintCache.put(LT(1), returnAST);
					} catch (RecognitionException e) {
						bag_constraintCache.put(LT(1), e);
						throw e;
					} catch (TokenStreamException e) {
						bag_constraintCache.put(LT(1), e);
						throw e;
					}
				}
			}
			
	IdentityHashMap subquery_constraintCache = new IdentityHashMap();
	public final void subquery_constraint() throws RecognitionException, TokenStreamException {
		Object retval = subquery_constraintCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST subquery_constraint_AST = null;
				
				boolean synPredMatched150 = false;
				if (((_tokenSet_0.member(LA(1))) && (_tokenSet_85.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (_tokenSet_99.member(LA(4))) && (_tokenSet_100.member(LA(5))) && (_tokenSet_101.member(LA(6))) && (_tokenSet_77.member(LA(7))))) {
					int _m150 = mark();
					synPredMatched150 = true;
					inputState.guessing++;
					try {
						{
						abstract_value();
						match(LITERAL_in);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched150 = false;
					}
					rewind(_m150);
inputState.guessing--;
				}
				if ( synPredMatched150 ) {
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(LITERAL_in);
					match(OPEN_PAREN);
					iql_statement();
					astFactory.addASTChild(currentAST, returnAST);
					match(CLOSE_PAREN);
					if ( inputState.guessing==0 ) {
						subquery_constraint_AST = (AST)currentAST.root;
						subquery_constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(SUBQUERY_CONSTRAINT,"SUBQUERY_CONSTRAINT")).add(subquery_constraint_AST));
						currentAST.root = subquery_constraint_AST;
						currentAST.child = subquery_constraint_AST!=null &&subquery_constraint_AST.getFirstChild()!=null ?
							subquery_constraint_AST.getFirstChild() : subquery_constraint_AST;
						currentAST.advanceChildToEnd();
					}
					subquery_constraint_AST = (AST)currentAST.root;
				}
				else if ((_tokenSet_0.member(LA(1))) && (_tokenSet_90.member(LA(2))) && (_tokenSet_73.member(LA(3))) && (_tokenSet_102.member(LA(4))) && (_tokenSet_74.member(LA(5))) && (_tokenSet_103.member(LA(6))) && (_tokenSet_76.member(LA(7)))) {
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					match(LITERAL_not);
					match(LITERAL_in);
					match(OPEN_PAREN);
					iql_statement();
					astFactory.addASTChild(currentAST, returnAST);
					match(CLOSE_PAREN);
					if ( inputState.guessing==0 ) {
						subquery_constraint_AST = (AST)currentAST.root;
						subquery_constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(NOT_CONSTRAINT,"NOT_CONSTRAINT")).add((AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(SUBQUERY_CONSTRAINT,"SUBQUERY_CONSTRAINT")).add(subquery_constraint_AST))));
						currentAST.root = subquery_constraint_AST;
						currentAST.child = subquery_constraint_AST!=null &&subquery_constraint_AST.getFirstChild()!=null ?
							subquery_constraint_AST.getFirstChild() : subquery_constraint_AST;
						currentAST.advanceChildToEnd();
					}
					subquery_constraint_AST = (AST)currentAST.root;
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				returnAST = subquery_constraint_AST;
				subquery_constraintCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				subquery_constraintCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				subquery_constraintCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap contains_constraintCache = new IdentityHashMap();
	public final void contains_constraint() throws RecognitionException, TokenStreamException {
		Object retval = contains_constraintCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST contains_constraint_AST = null;
				
				boolean synPredMatched161 = false;
				if (((LA(1)==QUESTION_MARK) && (LA(2)==DOT) && (LA(3)==IDENTIFIER) && (LA(4)==LITERAL_contains) && (LA(5)==QUESTION_MARK))) {
					int _m161 = mark();
					synPredMatched161 = true;
					inputState.guessing++;
					try {
						{
						collection_from_question_mark();
						match(LITERAL_contains);
						match(QUESTION_MARK);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched161 = false;
					}
					rewind(_m161);
inputState.guessing--;
				}
				if ( synPredMatched161 ) {
					collection_from_question_mark();
					astFactory.addASTChild(currentAST, returnAST);
					match(LITERAL_contains);
					AST tmp150_AST = null;
					tmp150_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp150_AST);
					match(QUESTION_MARK);
					if ( inputState.guessing==0 ) {
						contains_constraint_AST = (AST)currentAST.root;
						contains_constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(CONTAINS_CONSTRAINT,"CONTAINS_CONSTRAINT")).add(contains_constraint_AST));
						currentAST.root = contains_constraint_AST;
						currentAST.child = contains_constraint_AST!=null &&contains_constraint_AST.getFirstChild()!=null ?
							contains_constraint_AST.getFirstChild() : contains_constraint_AST;
						currentAST.advanceChildToEnd();
					}
					contains_constraint_AST = (AST)currentAST.root;
				}
				else {
					boolean synPredMatched163 = false;
					if (((LA(1)==QUESTION_MARK) && (LA(2)==DOT) && (LA(3)==IDENTIFIER) && (LA(4)==LITERAL_does) && (LA(5)==LITERAL_not) && (LA(6)==LITERAL_contain) && (LA(7)==QUESTION_MARK))) {
						int _m163 = mark();
						synPredMatched163 = true;
						inputState.guessing++;
						try {
							{
							collection_from_question_mark();
							match(LITERAL_does);
							match(LITERAL_not);
							match(LITERAL_contain);
							match(QUESTION_MARK);
							}
						}
						catch (RecognitionException pe) {
							synPredMatched163 = false;
						}
						rewind(_m163);
inputState.guessing--;
					}
					if ( synPredMatched163 ) {
						collection_from_question_mark();
						astFactory.addASTChild(currentAST, returnAST);
						match(LITERAL_does);
						match(LITERAL_not);
						match(LITERAL_contain);
						AST tmp154_AST = null;
						tmp154_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp154_AST);
						match(QUESTION_MARK);
						if ( inputState.guessing==0 ) {
							contains_constraint_AST = (AST)currentAST.root;
							contains_constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(NOT_CONSTRAINT,"NOT_CONSTRAINT")).add((AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(CONTAINS_CONSTRAINT,"CONTAINS_CONSTRAINT")).add(contains_constraint_AST))));
							currentAST.root = contains_constraint_AST;
							currentAST.child = contains_constraint_AST!=null &&contains_constraint_AST.getFirstChild()!=null ?
								contains_constraint_AST.getFirstChild() : contains_constraint_AST;
							currentAST.advanceChildToEnd();
						}
						contains_constraint_AST = (AST)currentAST.root;
					}
					else {
						boolean synPredMatched165 = false;
						if (((LA(1)==QUESTION_MARK) && (LA(2)==DOT) && (LA(3)==IDENTIFIER) && (LA(4)==LITERAL_contains) && (LA(5)==IDENTIFIER))) {
							int _m165 = mark();
							synPredMatched165 = true;
							inputState.guessing++;
							try {
								{
								collection_from_question_mark();
								match(LITERAL_contains);
								}
							}
							catch (RecognitionException pe) {
								synPredMatched165 = false;
							}
							rewind(_m165);
inputState.guessing--;
						}
						if ( synPredMatched165 ) {
							collection_from_question_mark();
							astFactory.addASTChild(currentAST, returnAST);
							match(LITERAL_contains);
							thing();
							astFactory.addASTChild(currentAST, returnAST);
							if ( inputState.guessing==0 ) {
								contains_constraint_AST = (AST)currentAST.root;
								contains_constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(CONTAINS_CONSTRAINT,"CONTAINS_CONSTRAINT")).add(contains_constraint_AST));
								currentAST.root = contains_constraint_AST;
								currentAST.child = contains_constraint_AST!=null &&contains_constraint_AST.getFirstChild()!=null ?
									contains_constraint_AST.getFirstChild() : contains_constraint_AST;
								currentAST.advanceChildToEnd();
							}
							contains_constraint_AST = (AST)currentAST.root;
						}
						else {
							boolean synPredMatched167 = false;
							if (((LA(1)==QUESTION_MARK) && (LA(2)==DOT) && (LA(3)==IDENTIFIER) && (LA(4)==LITERAL_does) && (LA(5)==LITERAL_not) && (LA(6)==LITERAL_contain) && (LA(7)==IDENTIFIER))) {
								int _m167 = mark();
								synPredMatched167 = true;
								inputState.guessing++;
								try {
									{
									collection_from_question_mark();
									match(LITERAL_does);
									match(LITERAL_not);
									match(LITERAL_contain);
									}
								}
								catch (RecognitionException pe) {
									synPredMatched167 = false;
								}
								rewind(_m167);
inputState.guessing--;
							}
							if ( synPredMatched167 ) {
								collection_from_question_mark();
								astFactory.addASTChild(currentAST, returnAST);
								match(LITERAL_does);
								match(LITERAL_not);
								match(LITERAL_contain);
								thing();
								astFactory.addASTChild(currentAST, returnAST);
								if ( inputState.guessing==0 ) {
									contains_constraint_AST = (AST)currentAST.root;
									contains_constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(NOT_CONSTRAINT,"NOT_CONSTRAINT")).add((AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(CONTAINS_CONSTRAINT,"CONTAINS_CONSTRAINT")).add(contains_constraint_AST))));
									currentAST.root = contains_constraint_AST;
									currentAST.child = contains_constraint_AST!=null &&contains_constraint_AST.getFirstChild()!=null ?
										contains_constraint_AST.getFirstChild() : contains_constraint_AST;
									currentAST.advanceChildToEnd();
								}
								contains_constraint_AST = (AST)currentAST.root;
							}
							else {
								boolean synPredMatched169 = false;
								if (((LA(1)==IDENTIFIER) && (LA(2)==DOT||LA(2)==LITERAL_contains) && (LA(3)==IDENTIFIER||LA(3)==QUESTION_MARK) && (_tokenSet_104.member(LA(4))) && (_tokenSet_79.member(LA(5))) && (_tokenSet_46.member(LA(6))) && (_tokenSet_44.member(LA(7))))) {
									int _m169 = mark();
									synPredMatched169 = true;
									inputState.guessing++;
									try {
										{
										thing();
										match(LITERAL_contains);
										match(QUESTION_MARK);
										}
									}
									catch (RecognitionException pe) {
										synPredMatched169 = false;
									}
									rewind(_m169);
inputState.guessing--;
								}
								if ( synPredMatched169 ) {
									thing();
									astFactory.addASTChild(currentAST, returnAST);
									match(LITERAL_contains);
									AST tmp160_AST = null;
									tmp160_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp160_AST);
									match(QUESTION_MARK);
									if ( inputState.guessing==0 ) {
										contains_constraint_AST = (AST)currentAST.root;
										contains_constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(CONTAINS_CONSTRAINT,"CONTAINS_CONSTRAINT")).add(contains_constraint_AST));
										currentAST.root = contains_constraint_AST;
										currentAST.child = contains_constraint_AST!=null &&contains_constraint_AST.getFirstChild()!=null ?
											contains_constraint_AST.getFirstChild() : contains_constraint_AST;
										currentAST.advanceChildToEnd();
									}
									contains_constraint_AST = (AST)currentAST.root;
								}
								else {
									boolean synPredMatched171 = false;
									if (((LA(1)==IDENTIFIER) && (LA(2)==DOT||LA(2)==LITERAL_contains) && (LA(3)==IDENTIFIER) && (_tokenSet_104.member(LA(4))) && (_tokenSet_79.member(LA(5))) && (_tokenSet_46.member(LA(6))) && (_tokenSet_44.member(LA(7))))) {
										int _m171 = mark();
										synPredMatched171 = true;
										inputState.guessing++;
										try {
											{
											thing();
											match(LITERAL_contains);
											}
										}
										catch (RecognitionException pe) {
											synPredMatched171 = false;
										}
										rewind(_m171);
inputState.guessing--;
									}
									if ( synPredMatched171 ) {
										thing();
										astFactory.addASTChild(currentAST, returnAST);
										match(LITERAL_contains);
										thing();
										astFactory.addASTChild(currentAST, returnAST);
										if ( inputState.guessing==0 ) {
											contains_constraint_AST = (AST)currentAST.root;
											contains_constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(CONTAINS_CONSTRAINT,"CONTAINS_CONSTRAINT")).add(contains_constraint_AST));
											currentAST.root = contains_constraint_AST;
											currentAST.child = contains_constraint_AST!=null &&contains_constraint_AST.getFirstChild()!=null ?
												contains_constraint_AST.getFirstChild() : contains_constraint_AST;
											currentAST.advanceChildToEnd();
										}
										contains_constraint_AST = (AST)currentAST.root;
									}
									else {
										boolean synPredMatched173 = false;
										if (((LA(1)==IDENTIFIER) && (LA(2)==DOT||LA(2)==LITERAL_does) && (LA(3)==IDENTIFIER||LA(3)==LITERAL_not) && (LA(4)==DOT||LA(4)==LITERAL_does||LA(4)==LITERAL_contain) && (LA(5)==IDENTIFIER||LA(5)==QUESTION_MARK||LA(5)==LITERAL_not) && (_tokenSet_105.member(LA(6))) && (_tokenSet_79.member(LA(7))))) {
											int _m173 = mark();
											synPredMatched173 = true;
											inputState.guessing++;
											try {
												{
												thing();
												match(LITERAL_does);
												match(LITERAL_not);
												match(LITERAL_contain);
												match(QUESTION_MARK);
												}
											}
											catch (RecognitionException pe) {
												synPredMatched173 = false;
											}
											rewind(_m173);
inputState.guessing--;
										}
										if ( synPredMatched173 ) {
											thing();
											astFactory.addASTChild(currentAST, returnAST);
											match(LITERAL_does);
											match(LITERAL_not);
											match(LITERAL_contain);
											AST tmp165_AST = null;
											tmp165_AST = astFactory.create(LT(1));
											astFactory.addASTChild(currentAST, tmp165_AST);
											match(QUESTION_MARK);
											if ( inputState.guessing==0 ) {
												contains_constraint_AST = (AST)currentAST.root;
												contains_constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(NOT_CONSTRAINT,"NOT_CONSTRAINT")).add((AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(CONTAINS_CONSTRAINT,"CONTAINS_CONSTRAINT")).add(contains_constraint_AST))));
												currentAST.root = contains_constraint_AST;
												currentAST.child = contains_constraint_AST!=null &&contains_constraint_AST.getFirstChild()!=null ?
													contains_constraint_AST.getFirstChild() : contains_constraint_AST;
												currentAST.advanceChildToEnd();
											}
											contains_constraint_AST = (AST)currentAST.root;
										}
										else if ((LA(1)==IDENTIFIER) && (LA(2)==DOT||LA(2)==LITERAL_does) && (LA(3)==IDENTIFIER||LA(3)==LITERAL_not) && (LA(4)==DOT||LA(4)==LITERAL_does||LA(4)==LITERAL_contain) && (LA(5)==IDENTIFIER||LA(5)==LITERAL_not) && (_tokenSet_105.member(LA(6))) && (_tokenSet_79.member(LA(7)))) {
											thing();
											astFactory.addASTChild(currentAST, returnAST);
											match(LITERAL_does);
											match(LITERAL_not);
											match(LITERAL_contain);
											thing();
											astFactory.addASTChild(currentAST, returnAST);
											if ( inputState.guessing==0 ) {
												contains_constraint_AST = (AST)currentAST.root;
												contains_constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(NOT_CONSTRAINT,"NOT_CONSTRAINT")).add((AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(CONTAINS_CONSTRAINT,"CONTAINS_CONSTRAINT")).add(contains_constraint_AST))));
												currentAST.root = contains_constraint_AST;
												currentAST.child = contains_constraint_AST!=null &&contains_constraint_AST.getFirstChild()!=null ?
													contains_constraint_AST.getFirstChild() : contains_constraint_AST;
												currentAST.advanceChildToEnd();
											}
											contains_constraint_AST = (AST)currentAST.root;
										}
										else {
											throw new NoViableAltException(LT(1), getFilename());
										}
										}}}}}}
										returnAST = contains_constraint_AST;
										contains_constraintCache.put(LT(1), returnAST);
									} catch (RecognitionException e) {
										contains_constraintCache.put(LT(1), e);
										throw e;
									} catch (TokenStreamException e) {
										contains_constraintCache.put(LT(1), e);
										throw e;
									}
								}
							}
							
	IdentityHashMap subquery_exists_constraintCache = new IdentityHashMap();
	public final void subquery_exists_constraint() throws RecognitionException, TokenStreamException {
		Object retval = subquery_exists_constraintCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST subquery_exists_constraint_AST = null;
				
				switch ( LA(1)) {
				case LITERAL_exists:
				{
					match(LITERAL_exists);
					match(OPEN_PAREN);
					iql_statement();
					astFactory.addASTChild(currentAST, returnAST);
					match(CLOSE_PAREN);
					if ( inputState.guessing==0 ) {
						subquery_exists_constraint_AST = (AST)currentAST.root;
						subquery_exists_constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(SUBQUERY_EXISTS_CONSTRAINT,"SUBQUERY_EXISTS_CONSTRAINT")).add(subquery_exists_constraint_AST));
						currentAST.root = subquery_exists_constraint_AST;
						currentAST.child = subquery_exists_constraint_AST!=null &&subquery_exists_constraint_AST.getFirstChild()!=null ?
							subquery_exists_constraint_AST.getFirstChild() : subquery_exists_constraint_AST;
						currentAST.advanceChildToEnd();
					}
					subquery_exists_constraint_AST = (AST)currentAST.root;
					break;
				}
				case LITERAL_does:
				{
					match(LITERAL_does);
					match(LITERAL_not);
					match(LITERAL_exist);
					match(OPEN_PAREN);
					iql_statement();
					astFactory.addASTChild(currentAST, returnAST);
					match(CLOSE_PAREN);
					if ( inputState.guessing==0 ) {
						subquery_exists_constraint_AST = (AST)currentAST.root;
						subquery_exists_constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(NOT_CONSTRAINT,"NOT_CONSTRAINT")).add((AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(SUBQUERY_EXISTS_CONSTRAINT,"SUBQUERY_EXISTS_CONSTRAINT")).add(subquery_exists_constraint_AST))));
						currentAST.root = subquery_exists_constraint_AST;
						currentAST.child = subquery_exists_constraint_AST!=null &&subquery_exists_constraint_AST.getFirstChild()!=null ?
							subquery_exists_constraint_AST.getFirstChild() : subquery_exists_constraint_AST;
						currentAST.advanceChildToEnd();
					}
					subquery_exists_constraint_AST = (AST)currentAST.root;
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				returnAST = subquery_exists_constraint_AST;
				subquery_exists_constraintCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				subquery_exists_constraintCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				subquery_exists_constraintCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap constraintCache = new IdentityHashMap();
	public final void constraint() throws RecognitionException, TokenStreamException {
		Object retval = constraintCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST constraint_AST = null;
				
				boolean synPredMatched130 = false;
				if (((_tokenSet_0.member(LA(1))) && (_tokenSet_106.member(LA(2))) && (_tokenSet_107.member(LA(3))) && (_tokenSet_108.member(LA(4))) && (_tokenSet_46.member(LA(5))) && (_tokenSet_44.member(LA(6))) && (_tokenSet_47.member(LA(7))))) {
					int _m130 = mark();
					synPredMatched130 = true;
					inputState.guessing++;
					try {
						{
						abstract_value();
						match(ISNULL);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched130 = false;
					}
					rewind(_m130);
inputState.guessing--;
				}
				if ( synPredMatched130 ) {
					abstract_value();
					astFactory.addASTChild(currentAST, returnAST);
					AST tmp177_AST = null;
					tmp177_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp177_AST);
					match(ISNULL);
					if ( inputState.guessing==0 ) {
						constraint_AST = (AST)currentAST.root;
						constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(CONSTRAINT,"CONSTRAINT")).add(constraint_AST));
						currentAST.root = constraint_AST;
						currentAST.child = constraint_AST!=null &&constraint_AST.getFirstChild()!=null ?
							constraint_AST.getFirstChild() : constraint_AST;
						currentAST.advanceChildToEnd();
					}
					constraint_AST = (AST)currentAST.root;
				}
				else {
					boolean synPredMatched132 = false;
					if (((_tokenSet_0.member(LA(1))) && (_tokenSet_109.member(LA(2))) && (_tokenSet_107.member(LA(3))) && (_tokenSet_110.member(LA(4))) && (_tokenSet_46.member(LA(5))) && (_tokenSet_44.member(LA(6))) && (_tokenSet_47.member(LA(7))))) {
						int _m132 = mark();
						synPredMatched132 = true;
						inputState.guessing++;
						try {
							{
							abstract_value();
							match(ISNOTNULL);
							}
						}
						catch (RecognitionException pe) {
							synPredMatched132 = false;
						}
						rewind(_m132);
inputState.guessing--;
					}
					if ( synPredMatched132 ) {
						abstract_value();
						astFactory.addASTChild(currentAST, returnAST);
						AST tmp178_AST = null;
						tmp178_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp178_AST);
						match(ISNOTNULL);
						if ( inputState.guessing==0 ) {
							constraint_AST = (AST)currentAST.root;
							constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(CONSTRAINT,"CONSTRAINT")).add(constraint_AST));
							currentAST.root = constraint_AST;
							currentAST.child = constraint_AST!=null &&constraint_AST.getFirstChild()!=null ?
								constraint_AST.getFirstChild() : constraint_AST;
							currentAST.advanceChildToEnd();
						}
						constraint_AST = (AST)currentAST.root;
					}
					else {
						boolean synPredMatched135 = false;
						if (((LA(1)==IDENTIFIER) && (LA(2)==DOT||LA(2)==EQ||LA(2)==NOT_EQ) && (LA(3)==IDENTIFIER||LA(3)==QUESTION_MARK) && (_tokenSet_111.member(LA(4))) && (_tokenSet_79.member(LA(5))) && (_tokenSet_46.member(LA(6))) && (_tokenSet_44.member(LA(7))))) {
							int _m135 = mark();
							synPredMatched135 = true;
							inputState.guessing++;
							try {
								{
								thing();
								{
								switch ( LA(1)) {
								case EQ:
								{
									match(EQ);
									break;
								}
								case NOT_EQ:
								{
									match(NOT_EQ);
									break;
								}
								default:
								{
									throw new NoViableAltException(LT(1), getFilename());
								}
								}
								}
								match(QUESTION_MARK);
								}
							}
							catch (RecognitionException pe) {
								synPredMatched135 = false;
							}
							rewind(_m135);
inputState.guessing--;
						}
						if ( synPredMatched135 ) {
							thing();
							astFactory.addASTChild(currentAST, returnAST);
							{
							switch ( LA(1)) {
							case EQ:
							{
								AST tmp179_AST = null;
								tmp179_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp179_AST);
								match(EQ);
								break;
							}
							case NOT_EQ:
							{
								AST tmp180_AST = null;
								tmp180_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp180_AST);
								match(NOT_EQ);
								break;
							}
							default:
							{
								throw new NoViableAltException(LT(1), getFilename());
							}
							}
							}
							AST tmp181_AST = null;
							tmp181_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp181_AST);
							match(QUESTION_MARK);
							if ( inputState.guessing==0 ) {
								constraint_AST = (AST)currentAST.root;
								constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(CONSTRAINT,"CONSTRAINT")).add(constraint_AST));
								currentAST.root = constraint_AST;
								currentAST.child = constraint_AST!=null &&constraint_AST.getFirstChild()!=null ?
									constraint_AST.getFirstChild() : constraint_AST;
								currentAST.advanceChildToEnd();
							}
							constraint_AST = (AST)currentAST.root;
						}
						else if ((_tokenSet_0.member(LA(1))) && (_tokenSet_112.member(LA(2))) && (_tokenSet_113.member(LA(3))) && (_tokenSet_114.member(LA(4))) && (_tokenSet_115.member(LA(5))) && (_tokenSet_46.member(LA(6))) && (_tokenSet_44.member(LA(7)))) {
							abstract_value();
							astFactory.addASTChild(currentAST, returnAST);
							comparison_op();
							astFactory.addASTChild(currentAST, returnAST);
							abstract_value();
							astFactory.addASTChild(currentAST, returnAST);
							if ( inputState.guessing==0 ) {
								constraint_AST = (AST)currentAST.root;
								constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(CONSTRAINT,"CONSTRAINT")).add(constraint_AST));
								currentAST.root = constraint_AST;
								currentAST.child = constraint_AST!=null &&constraint_AST.getFirstChild()!=null ?
									constraint_AST.getFirstChild() : constraint_AST;
								currentAST.advanceChildToEnd();
							}
							constraint_AST = (AST)currentAST.root;
						}
						else {
							throw new NoViableAltException(LT(1), getFilename());
						}
						}}
						returnAST = constraint_AST;
						constraintCache.put(LT(1), returnAST);
					} catch (RecognitionException e) {
						constraintCache.put(LT(1), e);
						throw e;
					} catch (TokenStreamException e) {
						constraintCache.put(LT(1), e);
						throw e;
					}
				}
			}
			
	IdentityHashMap not_constraintCache = new IdentityHashMap();
	public final void not_constraint() throws RecognitionException, TokenStreamException {
		Object retval = not_constraintCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST not_constraint_AST = null;
				
				match(LITERAL_not);
				safe_abstract_constraint();
				astFactory.addASTChild(currentAST, returnAST);
				if ( inputState.guessing==0 ) {
					not_constraint_AST = (AST)currentAST.root;
					not_constraint_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(NOT_CONSTRAINT,"NOT_CONSTRAINT")).add(not_constraint_AST));
					currentAST.root = not_constraint_AST;
					currentAST.child = not_constraint_AST!=null &&not_constraint_AST.getFirstChild()!=null ?
						not_constraint_AST.getFirstChild() : not_constraint_AST;
					currentAST.advanceChildToEnd();
				}
				not_constraint_AST = (AST)currentAST.root;
				returnAST = not_constraint_AST;
				not_constraintCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				not_constraintCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				not_constraintCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap comparison_opCache = new IdentityHashMap();
	public final void comparison_op() throws RecognitionException, TokenStreamException {
		Object retval = comparison_opCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST comparison_op_AST = null;
				
				switch ( LA(1)) {
				case EQ:
				{
					AST tmp183_AST = null;
					tmp183_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp183_AST);
					match(EQ);
					comparison_op_AST = (AST)currentAST.root;
					break;
				}
				case LT:
				{
					AST tmp184_AST = null;
					tmp184_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp184_AST);
					match(LT);
					comparison_op_AST = (AST)currentAST.root;
					break;
				}
				case GT:
				{
					AST tmp185_AST = null;
					tmp185_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp185_AST);
					match(GT);
					comparison_op_AST = (AST)currentAST.root;
					break;
				}
				case NOT_EQ:
				{
					AST tmp186_AST = null;
					tmp186_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp186_AST);
					match(NOT_EQ);
					comparison_op_AST = (AST)currentAST.root;
					break;
				}
				case LE:
				{
					AST tmp187_AST = null;
					tmp187_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp187_AST);
					match(LE);
					comparison_op_AST = (AST)currentAST.root;
					break;
				}
				case GE:
				{
					AST tmp188_AST = null;
					tmp188_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp188_AST);
					match(GE);
					comparison_op_AST = (AST)currentAST.root;
					break;
				}
				case LITERAL_like:
				{
					AST tmp189_AST = null;
					tmp189_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp189_AST);
					match(LITERAL_like);
					comparison_op_AST = (AST)currentAST.root;
					break;
				}
				case LITERAL_not:
				{
					AST tmp190_AST = null;
					tmp190_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp190_AST);
					match(LITERAL_not);
					AST tmp191_AST = null;
					tmp191_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp191_AST);
					match(LITERAL_like);
					if ( inputState.guessing==0 ) {
						comparison_op_AST = (AST)currentAST.root;
						comparison_op_AST = astFactory.create(NOTLIKE,"NOTLIKE");
						currentAST.root = comparison_op_AST;
						currentAST.child = comparison_op_AST!=null &&comparison_op_AST.getFirstChild()!=null ?
							comparison_op_AST.getFirstChild() : comparison_op_AST;
						currentAST.advanceChildToEnd();
					}
					comparison_op_AST = (AST)currentAST.root;
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				returnAST = comparison_op_AST;
				comparison_opCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				comparison_opCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				comparison_opCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap or_constraint_setCache = new IdentityHashMap();
	public final void or_constraint_set() throws RecognitionException, TokenStreamException {
		Object retval = or_constraint_setCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST or_constraint_set_AST = null;
				
				safe_abstract_constraint();
				astFactory.addASTChild(currentAST, returnAST);
				{
				int _cnt144=0;
				_loop144:
				do {
					if ((LA(1)==LITERAL_or)) {
						match(LITERAL_or);
						safe_abstract_constraint();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						if ( _cnt144>=1 ) { break _loop144; } else {throw new NoViableAltException(LT(1), getFilename());}
					}
					
					_cnt144++;
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					or_constraint_set_AST = (AST)currentAST.root;
					or_constraint_set_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(OR_CONSTRAINT_SET,"OR_CONSTRAINT_SET")).add(or_constraint_set_AST));
					currentAST.root = or_constraint_set_AST;
					currentAST.child = or_constraint_set_AST!=null &&or_constraint_set_AST.getFirstChild()!=null ?
						or_constraint_set_AST.getFirstChild() : or_constraint_set_AST;
					currentAST.advanceChildToEnd();
				}
				or_constraint_set_AST = (AST)currentAST.root;
				returnAST = or_constraint_set_AST;
				or_constraint_setCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				or_constraint_setCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				or_constraint_setCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap and_constraint_setCache = new IdentityHashMap();
	public final void and_constraint_set() throws RecognitionException, TokenStreamException {
		Object retval = and_constraint_setCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST and_constraint_set_AST = null;
				
				safe_abstract_constraint();
				astFactory.addASTChild(currentAST, returnAST);
				{
				int _cnt147=0;
				_loop147:
				do {
					if ((LA(1)==LITERAL_and)) {
						match(LITERAL_and);
						safe_abstract_constraint();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						if ( _cnt147>=1 ) { break _loop147; } else {throw new NoViableAltException(LT(1), getFilename());}
					}
					
					_cnt147++;
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					and_constraint_set_AST = (AST)currentAST.root;
					and_constraint_set_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(AND_CONSTRAINT_SET,"AND_CONSTRAINT_SET")).add(and_constraint_set_AST));
					currentAST.root = and_constraint_set_AST;
					currentAST.child = and_constraint_set_AST!=null &&and_constraint_set_AST.getFirstChild()!=null ?
						and_constraint_set_AST.getFirstChild() : and_constraint_set_AST;
					currentAST.advanceChildToEnd();
				}
				and_constraint_set_AST = (AST)currentAST.root;
				returnAST = and_constraint_set_AST;
				and_constraint_setCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				and_constraint_setCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				and_constraint_setCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	IdentityHashMap collection_from_question_markCache = new IdentityHashMap();
	public final void collection_from_question_mark() throws RecognitionException, TokenStreamException {
		Object retval = collection_from_question_markCache.get(LT(1));
		if (retval instanceof RecognitionException) {
			throw (RecognitionException) retval;
		} else if (retval instanceof TokenStreamException) {
			throw (TokenStreamException) retval;
		} else if ((retval instanceof AST) && (inputState.guessing != 0)) {
			returnAST = (AST) retval;
		} else {
			try {
				
				returnAST = null;
				ASTPair currentAST = new ASTPair();
				AST collection_from_question_mark_AST = null;
				
				AST tmp194_AST = null;
				tmp194_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp194_AST);
				match(QUESTION_MARK);
				match(DOT);
				AST tmp196_AST = null;
				tmp196_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp196_AST);
				match(IDENTIFIER);
				if ( inputState.guessing==0 ) {
					collection_from_question_mark_AST = (AST)currentAST.root;
					collection_from_question_mark_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(FIELD,"FIELD")).add(collection_from_question_mark_AST));
					currentAST.root = collection_from_question_mark_AST;
					currentAST.child = collection_from_question_mark_AST!=null &&collection_from_question_mark_AST.getFirstChild()!=null ?
						collection_from_question_mark_AST.getFirstChild() : collection_from_question_mark_AST;
					currentAST.advanceChildToEnd();
				}
				collection_from_question_mark_AST = (AST)currentAST.root;
				returnAST = collection_from_question_mark_AST;
				collection_from_question_markCache.put(LT(1), returnAST);
			} catch (RecognitionException e) {
				collection_from_question_markCache.put(LT(1), e);
				throw e;
			} catch (TokenStreamException e) {
				collection_from_question_markCache.put(LT(1), e);
				throw e;
			}
		}
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"IQL_STATEMENT",
		"SELECT_LIST",
		"FROM_LIST",
		"WHERE_CLAUSE",
		"GROUP_CLAUSE",
		"ORDER_CLAUSE",
		"SELECT_VALUE",
		"TABLE_ALIAS",
		"FIELD_ALIAS",
		"TABLE",
		"TABLE_NAME",
		"SUBQUERY",
		"SUBQUERY_LIMIT",
		"CONSTANT",
		"FIELD",
		"FIELD_NAME",
		"SAFE_FUNCTION",
		"UNSAFE_FUNCTION",
		"CONSTRAINT",
		"NOT_CONSTRAINT",
		"AND_CONSTRAINT_SET",
		"OR_CONSTRAINT_SET",
		"SUBQUERY_CONSTRAINT",
		"SUBQUERY_EXISTS_CONSTRAINT",
		"CONTAINS_CONSTRAINT",
		"NOTLIKE",
		"BAG_CONSTRAINT",
		"TYPECAST",
		"FIELD_PATH_EXPRESSION",
		"OBJECTSTOREBAG",
		"ORDER_DESC",
		"BAGS_FOR",
		"COLLECTION_SELECT_LIST",
		"COLLECTION_SELECT_VALUE",
		"\"explain\"",
		"\"select\"",
		"\"all\"",
		"\"distinct\"",
		"COMMA",
		"\"from\"",
		"\"where\"",
		"\"group\"",
		"\"by\"",
		"\"order\"",
		"\"as\"",
		"IDENTIFIER",
		"DOT",
		"OPEN_PAREN",
		"\"union\"",
		"\"intersect\"",
		"\"except\"",
		"\"allbutintersect\"",
		"\"desc\"",
		"COLONTYPE",
		"CLOSE_PAREN",
		"\"bag\"",
		"INTEGER",
		"\"bags\"",
		"\"for\"",
		"\"in\"",
		"QUESTION_MARK",
		"\"limit\"",
		"QUOTED_STRING",
		"FLOAT",
		"\"true\"",
		"\"false\"",
		"\"null\"",
		"\"singleton\"",
		"\"count\"",
		"ASTERISK",
		"\"max\"",
		"\"min\"",
		"\"sum\"",
		"\"avg\"",
		"\"substr\"",
		"\"indexof\"",
		"\"lower\"",
		"\"upper\"",
		"\"stddev\"",
		"PLUS",
		"PERCENT",
		"DIVIDE",
		"POWER",
		"MINUS",
		"ISNULL",
		"ISNOTNULL",
		"EQ",
		"NOT_EQ",
		"\"not\"",
		"\"or\"",
		"\"and\"",
		"\"exists\"",
		"\"does\"",
		"\"exist\"",
		"\"contains\"",
		"\"contain\"",
		"LT",
		"GT",
		"LE",
		"GE",
		"\"like\"",
		"SEMI",
		"AT_SIGN",
		"VERTBAR",
		"WS"
	};
	
	protected void buildTokenTypeASTClassMap() {
		tokenTypeToASTClassMap=null;
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 1155736254373953536L, 523644L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 1300977342356652032L, 16777084L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 1589207718508363776L, 16777084L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 1589493591531585536L, 16777084L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { 1589212116554874880L, 16777084L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { 1589695901671096322L, 16777086L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = { 4472043643653324802L, 8338276223L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = { 1300977342356652032L, 523644L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = { 288257314186592256L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = { 1732478481654087680L, 6711410173L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	private static final long[] mk_tokenSet_10() {
		long[] data = { -7633851232410402816L, 2156073582461L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	private static final long[] mk_tokenSet_11() {
		long[] data = { 289844459221286914L, 2L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
	private static final long[] mk_tokenSet_12() {
		long[] data = { 4326798157624115202L, 8322022783L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
	private static final long[] mk_tokenSet_13() {
		long[] data = { -3021918923378393086L, 2156073582463L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());
	private static final long[] mk_tokenSet_14() {
		long[] data = { -4679240287715852286L, 2164663517055L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());
	private static final long[] mk_tokenSet_15() {
		long[] data = { -67554269288464382L, 2199023255423L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());
	private static final long[] mk_tokenSet_16() {
		long[] data = { -274877906942L, 2199023255423L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_16 = new BitSet(mk_tokenSet_16());
	private static final long[] mk_tokenSet_17() {
		long[] data = { 1152921504606846976L, 124L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_17 = new BitSet(mk_tokenSet_17());
	private static final long[] mk_tokenSet_18() {
		long[] data = { 0L, 523520L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_18 = new BitSet(mk_tokenSet_18());
	private static final long[] mk_tokenSet_19() {
		long[] data = { 1155736254373953536L, 524156L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_19 = new BitSet(mk_tokenSet_19());
	private static final long[] mk_tokenSet_20() {
		long[] data = { 1589489193485074432L, 16777084L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_20 = new BitSet(mk_tokenSet_20());
	private static final long[] mk_tokenSet_21() {
		long[] data = { 290398613081686018L, 2L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_21 = new BitSet(mk_tokenSet_21());
	private static final long[] mk_tokenSet_22() {
		long[] data = { 2022111834642841602L, 8322022783L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_22 = new BitSet(mk_tokenSet_22());
	private static final long[] mk_tokenSet_23() {
		long[] data = { 433475862180921344L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_23 = new BitSet(mk_tokenSet_23());
	private static final long[] mk_tokenSet_24() {
		long[] data = { 289923624058486784L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_24 = new BitSet(mk_tokenSet_24());
	private static final long[] mk_tokenSet_25() {
		long[] data = { 1155736254373953536L, 6711410045L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_25 = new BitSet(mk_tokenSet_25());
	private static final long[] mk_tokenSet_26() {
		long[] data = { -7922394694498123776L, 2156073582461L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_26 = new BitSet(mk_tokenSet_26());
	private static final long[] mk_tokenSet_27() {
		long[] data = { -7057702741409267712L, 2164663517053L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_27 = new BitSet(mk_tokenSet_27());
	private static final long[] mk_tokenSet_28() {
		long[] data = { -4751676113753735166L, 2199023255423L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_28 = new BitSet(mk_tokenSet_28());
	private static final long[] mk_tokenSet_29() {
		long[] data = { -139611863326392318L, 2199023255423L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_29 = new BitSet(mk_tokenSet_29());
	private static final long[] mk_tokenSet_30() {
		long[] data = { -7633988396485967870L, 2154462969727L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_30 = new BitSet(mk_tokenSet_30());
	private static final long[] mk_tokenSet_31() {
		long[] data = { -7057144189502357502L, 2164663517055L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_31 = new BitSet(mk_tokenSet_31());
	private static final long[] mk_tokenSet_32() {
		long[] data = { -4751297881753780222L, 2199023255423L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_32 = new BitSet(mk_tokenSet_32());
	private static final long[] mk_tokenSet_33() {
		long[] data = { -7561926404401528830L, 2132451262334L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_33 = new BitSet(mk_tokenSet_33());
	private static final long[] mk_tokenSet_34() {
		long[] data = { -6985087420098150398L, 2138893713279L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_34 = new BitSet(mk_tokenSet_34());
	private static final long[] mk_tokenSet_35() {
		long[] data = { -6985086595464429566L, 2156073582463L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_35 = new BitSet(mk_tokenSet_35());
	private static final long[] mk_tokenSet_36() {
		long[] data = { -8862903746758180862L, 2132434485250L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_36 = new BitSet(mk_tokenSet_36());
	private static final long[] mk_tokenSet_37() {
		long[] data = { -8861777846851338238L, 2132434485250L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_37 = new BitSet(mk_tokenSet_37());
	private static final long[] mk_tokenSet_38() {
		long[] data = { -6986213320004993022L, 2138893713279L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_38 = new BitSet(mk_tokenSet_38());
	private static final long[] mk_tokenSet_39() {
		long[] data = { 1373034936394579968L, 16777084L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_39 = new BitSet(mk_tokenSet_39());
	private static final long[] mk_tokenSet_40() {
		long[] data = { 1589212116554874882L, 16777086L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_40 = new BitSet(mk_tokenSet_40());
	private static final long[] mk_tokenSet_41() {
		long[] data = { 1661727107429957634L, 1627389822L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_41 = new BitSet(mk_tokenSet_41());
	private static final long[] mk_tokenSet_42() {
		long[] data = { 1661823864453201922L, 8338276223L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_42 = new BitSet(mk_tokenSet_42());
	private static final long[] mk_tokenSet_43() {
		long[] data = { -6985087420098150398L, 2156073582463L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_43 = new BitSet(mk_tokenSet_43());
	private static final long[] mk_tokenSet_44() {
		long[] data = { -6985086595464429566L, 2164663517055L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_44 = new BitSet(mk_tokenSet_44());
	private static final long[] mk_tokenSet_45() {
		long[] data = { 1589665115345518594L, 1627389822L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_45 = new BitSet(mk_tokenSet_45());
	private static final long[] mk_tokenSet_46() {
		long[] data = { -7057145014136078334L, 2156073582463L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_46 = new BitSet(mk_tokenSet_46());
	private static final long[] mk_tokenSet_47() {
		long[] data = { -4679240287715852286L, 2199023255423L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_47 = new BitSet(mk_tokenSet_47());
	private static final long[] mk_tokenSet_48() {
		long[] data = { 1300977342356652032L, 1047932L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_48 = new BitSet(mk_tokenSet_48());
	private static final long[] mk_tokenSet_49() {
		long[] data = { -7561618541145751550L, 2132451262334L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_49 = new BitSet(mk_tokenSet_49());
	private static final long[] mk_tokenSet_50() {
		long[] data = { 1300977342356652032L, 1572220L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_50 = new BitSet(mk_tokenSet_50());
	private static final long[] mk_tokenSet_51() {
		long[] data = { 1300977342356652032L, 524156L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_51 = new BitSet(mk_tokenSet_51());
	private static final long[] mk_tokenSet_52() {
		long[] data = { 1300977342356652032L, 2620796L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_52 = new BitSet(mk_tokenSet_52());
	private static final long[] mk_tokenSet_53() {
		long[] data = { 1300977342356652032L, 4717948L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_53 = new BitSet(mk_tokenSet_53());
	private static final long[] mk_tokenSet_54() {
		long[] data = { 1300977342356652032L, 8912252L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_54 = new BitSet(mk_tokenSet_54());
	private static final long[] mk_tokenSet_55() {
		long[] data = { -8862595883502403582L, 2132450738690L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_55 = new BitSet(mk_tokenSet_55());
	private static final long[] mk_tokenSet_56() {
		long[] data = { -8861469983595560958L, 2132450738690L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_56 = new BitSet(mk_tokenSet_56());
	private static final long[] mk_tokenSet_57() {
		long[] data = { 1589238504833941504L, 16777084L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_57 = new BitSet(mk_tokenSet_57());
	private static final long[] mk_tokenSet_58() {
		long[] data = { 2165980732114075648L, 6727663485L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_58 = new BitSet(mk_tokenSet_58());
	private static final long[] mk_tokenSet_59() {
		long[] data = { -7057390480106979328L, 2156073582461L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_59 = new BitSet(mk_tokenSet_59());
	private static final long[] mk_tokenSet_60() {
		long[] data = { -4751368250497957886L, 2164663517055L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_60 = new BitSet(mk_tokenSet_60());
	private static final long[] mk_tokenSet_61() {
		long[] data = { 2020458169154666496L, 6711410173L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_61 = new BitSet(mk_tokenSet_61());
	private static final long[] mk_tokenSet_62() {
		long[] data = { 289387062384132096L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_62 = new BitSet(mk_tokenSet_62());
	private static final long[] mk_tokenSet_63() {
		long[] data = { 2020739644131377152L, 6711410045L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_63 = new BitSet(mk_tokenSet_63());
	private static final long[] mk_tokenSet_64() {
		long[] data = { 288261162477289472L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_64 = new BitSet(mk_tokenSet_64());
	private static final long[] mk_tokenSet_65() {
		long[] data = { -7922394694498123776L, 2154999840637L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_65 = new BitSet(mk_tokenSet_65());
	private static final long[] mk_tokenSet_66() {
		long[] data = { -7922394694498123776L, 2155536711549L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_66 = new BitSet(mk_tokenSet_66());
	private static final long[] mk_tokenSet_67() {
		long[] data = { -7634164318346412032L, 2156073582461L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_67 = new BitSet(mk_tokenSet_67());
	private static final long[] mk_tokenSet_68() {
		long[] data = { -7057526819548823550L, 2164663517055L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_68 = new BitSet(mk_tokenSet_68());
	private static final long[] mk_tokenSet_69() {
		long[] data = { -7922394694498123776L, 285212540L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_69 = new BitSet(mk_tokenSet_69());
	private static final long[] mk_tokenSet_70() {
		long[] data = { -7057703566042988544L, 16777085L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_70 = new BitSet(mk_tokenSet_70());
	private static final long[] mk_tokenSet_71() {
		long[] data = { -7057523246136033278L, 1895825279L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_71 = new BitSet(mk_tokenSet_71());
	private static final long[] mk_tokenSet_72() {
		long[] data = { -7057145014136078334L, 8338276223L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_72 = new BitSet(mk_tokenSet_72());
	private static final long[] mk_tokenSet_73() {
		long[] data = { -7634164318346412032L, 16777084L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_73 = new BitSet(mk_tokenSet_73());
	private static final long[] mk_tokenSet_74() {
		long[] data = { -7634159095666180096L, 285212540L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_74 = new BitSet(mk_tokenSet_74());
	private static final long[] mk_tokenSet_75() {
		long[] data = { -4751852035614179328L, 285212540L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_75 = new BitSet(mk_tokenSet_75());
	private static final long[] mk_tokenSet_76() {
		long[] data = { -139682232070569984L, 285212540L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_76 = new BitSet(mk_tokenSet_76());
	private static final long[] mk_tokenSet_77() {
		long[] data = { -139611863326392318L, 8338276223L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_77 = new BitSet(mk_tokenSet_77());
	private static final long[] mk_tokenSet_78() {
		long[] data = { 289532197918998530L, 57445187586L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_78 = new BitSet(mk_tokenSet_78());
	private static final long[] mk_tokenSet_79() {
		long[] data = { 1444525182432575490L, 8322022783L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_79 = new BitSet(mk_tokenSet_79());
	private static final long[] mk_tokenSet_80() {
		long[] data = { -7057145014136078334L, 2190433320831L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_80 = new BitSet(mk_tokenSet_80());
	private static final long[] mk_tokenSet_81() {
		long[] data = { 288406298012155906L, 1610612738L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_81 = new BitSet(mk_tokenSet_81());
	private static final long[] mk_tokenSet_82() {
		long[] data = { 1300977342356652032L, 2130840649596L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_82 = new BitSet(mk_tokenSet_82());
	private static final long[] mk_tokenSet_83() {
		long[] data = { 1589383640368807938L, 1101139017599L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_83 = new BitSet(mk_tokenSet_83());
	private static final long[] mk_tokenSet_84() {
		long[] data = { 1589766270415273986L, 2138893713279L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_84 = new BitSet(mk_tokenSet_84());
	private static final long[] mk_tokenSet_85() {
		long[] data = { -7922394694498123776L, 16777084L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_85 = new BitSet(mk_tokenSet_85());
	private static final long[] mk_tokenSet_86() {
		long[] data = { 2165668470811787264L, 16777084L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_86 = new BitSet(mk_tokenSet_86());
	private static final long[] mk_tokenSet_87() {
		long[] data = { -7634159920299900928L, 16777084L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_87 = new BitSet(mk_tokenSet_87());
	private static final long[] mk_tokenSet_88() {
		long[] data = { -7057699167996477440L, 16777084L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_88 = new BitSet(mk_tokenSet_88());
	private static final long[] mk_tokenSet_89() {
		long[] data = { -7057523246136033278L, 1627389822L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_89 = new BitSet(mk_tokenSet_89());
	private static final long[] mk_tokenSet_90() {
		long[] data = { 1300977342356652032L, 285212540L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_90 = new BitSet(mk_tokenSet_90());
	private static final long[] mk_tokenSet_91() {
		long[] data = { 2165672868858298368L, 285212540L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_91 = new BitSet(mk_tokenSet_91());
	private static final long[] mk_tokenSet_92() {
		long[] data = { -7634159920299900928L, 285212540L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_92 = new BitSet(mk_tokenSet_92());
	private static final long[] mk_tokenSet_93() {
		long[] data = { -7057699167996477440L, 285212540L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_93 = new BitSet(mk_tokenSet_93());
	private static final long[] mk_tokenSet_94() {
		long[] data = { 1589207718508363776L, 16777085L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_94 = new BitSet(mk_tokenSet_94());
	private static final long[] mk_tokenSet_95() {
		long[] data = { -7633983998439456766L, 1627389822L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_95 = new BitSet(mk_tokenSet_95());
	private static final long[] mk_tokenSet_96() {
		long[] data = { -7633605766439501822L, 8338276223L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_96 = new BitSet(mk_tokenSet_96());
	private static final long[] mk_tokenSet_97() {
		long[] data = { 1589212116554874880L, 285212541L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_97 = new BitSet(mk_tokenSet_97());
	private static final long[] mk_tokenSet_98() {
		long[] data = { -7633983998439456766L, 1895825278L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_98 = new BitSet(mk_tokenSet_98());
	private static final long[] mk_tokenSet_99() {
		long[] data = { -7634159095666180096L, 16777084L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_99 = new BitSet(mk_tokenSet_99());
	private static final long[] mk_tokenSet_100() {
		long[] data = { -4751852310492086272L, 16777084L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_100 = new BitSet(mk_tokenSet_100());
	private static final long[] mk_tokenSet_101() {
		long[] data = { -139682232070569984L, 16777084L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_101 = new BitSet(mk_tokenSet_101());
	private static final long[] mk_tokenSet_102() {
		long[] data = { 1589212116554874880L, 285212540L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_102 = new BitSet(mk_tokenSet_102());
	private static final long[] mk_tokenSet_103() {
		long[] data = { -4751852310492086272L, 285212540L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_103 = new BitSet(mk_tokenSet_103());
	private static final long[] mk_tokenSet_104() {
		long[] data = { 289532197918998530L, 18790481922L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_104 = new BitSet(mk_tokenSet_104());
	private static final long[] mk_tokenSet_105() {
		long[] data = { 289532197918998530L, 40265318402L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_105 = new BitSet(mk_tokenSet_105());
	private static final long[] mk_tokenSet_106() {
		long[] data = { 1300977342356652032L, 33554300L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_106 = new BitSet(mk_tokenSet_106());
	private static final long[] mk_tokenSet_107() {
		long[] data = { 1589383640368807938L, 1627389822L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_107 = new BitSet(mk_tokenSet_107());
	private static final long[] mk_tokenSet_108() {
		long[] data = { 1589766270415273986L, 8355053439L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_108 = new BitSet(mk_tokenSet_108());
	private static final long[] mk_tokenSet_109() {
		long[] data = { 1300977342356652032L, 50331516L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_109 = new BitSet(mk_tokenSet_109());
	private static final long[] mk_tokenSet_110() {
		long[] data = { 1589766270415273986L, 8371830655L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_110 = new BitSet(mk_tokenSet_110());
	private static final long[] mk_tokenSet_111() {
		long[] data = { 289532197918998530L, 1811939330L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_111 = new BitSet(mk_tokenSet_111());
	private static final long[] mk_tokenSet_112() {
		long[] data = { 1300977342356652032L, 2130790317948L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_112 = new BitSet(mk_tokenSet_112());
	private static final long[] mk_tokenSet_113() {
		long[] data = { 1589207718508363776L, 1099528404860L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_113 = new BitSet(mk_tokenSet_113());
	private static final long[] mk_tokenSet_114() {
		long[] data = { 1589388038415319042L, 2132400930686L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_114 = new BitSet(mk_tokenSet_114());
	private static final long[] mk_tokenSet_115() {
		long[] data = { 1589766270415273986L, 2138843381631L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_115 = new BitSet(mk_tokenSet_115());
	
	}
