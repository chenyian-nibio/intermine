// $ANTLR 2.7.6 (20060327): "intermine_iql.g" -> "IqlLexer.java"$

package org.intermine.objectstore.query.iql;

import java.io.InputStream;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.TokenStreamRecognitionException;
import antlr.CharStreamException;
import antlr.CharStreamIOException;
import antlr.ANTLRException;
import java.io.Reader;
import java.util.Hashtable;
import antlr.CharScanner;
import antlr.InputBuffer;
import antlr.ByteBuffer;
import antlr.CharBuffer;
import antlr.Token;
import antlr.CommonToken;
import antlr.RecognitionException;
import antlr.NoViableAltForCharException;
import antlr.MismatchedCharException;
import antlr.TokenStream;
import antlr.ANTLRHashString;
import antlr.LexerSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.SemanticException;

public class IqlLexer extends antlr.CharScanner implements IqlTokenTypes, TokenStream
 {
public IqlLexer(InputStream in) {
	this(new ByteBuffer(in));
}
public IqlLexer(Reader in) {
	this(new CharBuffer(in));
}
public IqlLexer(InputBuffer ib) {
	this(new LexerSharedInputState(ib));
}
public IqlLexer(LexerSharedInputState state) {
	super(state);
	caseSensitiveLiterals = false;
	setCaseSensitive(false);
	literals = new Hashtable();
	literals.put(new ANTLRHashString("all", this), new Integer(40));
	literals.put(new ANTLRHashString("count", this), new Integer(72));
	literals.put(new ANTLRHashString("sum", this), new Integer(76));
	literals.put(new ANTLRHashString("except", this), new Integer(54));
	literals.put(new ANTLRHashString("for", this), new Integer(62));
	literals.put(new ANTLRHashString("min", this), new Integer(75));
	literals.put(new ANTLRHashString("lower", this), new Integer(80));
	literals.put(new ANTLRHashString("upper", this), new Integer(81));
	literals.put(new ANTLRHashString("exist", this), new Integer(97));
	literals.put(new ANTLRHashString("false", this), new Integer(69));
	literals.put(new ANTLRHashString("bag", this), new Integer(59));
	literals.put(new ANTLRHashString("true", this), new Integer(68));
	literals.put(new ANTLRHashString("and", this), new Integer(94));
	literals.put(new ANTLRHashString("desc", this), new Integer(56));
	literals.put(new ANTLRHashString("select", this), new Integer(39));
	literals.put(new ANTLRHashString("intersect", this), new Integer(53));
	literals.put(new ANTLRHashString("bags", this), new Integer(61));
	literals.put(new ANTLRHashString("exists", this), new Integer(95));
	literals.put(new ANTLRHashString("distinct", this), new Integer(41));
	literals.put(new ANTLRHashString("does", this), new Integer(96));
	literals.put(new ANTLRHashString("group", this), new Integer(45));
	literals.put(new ANTLRHashString("where", this), new Integer(44));
	literals.put(new ANTLRHashString("avg", this), new Integer(77));
	literals.put(new ANTLRHashString("order", this), new Integer(47));
	literals.put(new ANTLRHashString("limit", this), new Integer(65));
	literals.put(new ANTLRHashString("in", this), new Integer(63));
	literals.put(new ANTLRHashString("null", this), new Integer(70));
	literals.put(new ANTLRHashString("union", this), new Integer(52));
	literals.put(new ANTLRHashString("or", this), new Integer(93));
	literals.put(new ANTLRHashString("stddev", this), new Integer(82));
	literals.put(new ANTLRHashString("max", this), new Integer(74));
	literals.put(new ANTLRHashString("from", this), new Integer(43));
	literals.put(new ANTLRHashString("allbutintersect", this), new Integer(55));
	literals.put(new ANTLRHashString("contain", this), new Integer(99));
	literals.put(new ANTLRHashString("indexof", this), new Integer(79));
	literals.put(new ANTLRHashString("singleton", this), new Integer(71));
	literals.put(new ANTLRHashString("like", this), new Integer(104));
	literals.put(new ANTLRHashString("substr", this), new Integer(78));
	literals.put(new ANTLRHashString("contains", this), new Integer(98));
	literals.put(new ANTLRHashString("not", this), new Integer(92));
	literals.put(new ANTLRHashString("explain", this), new Integer(38));
	literals.put(new ANTLRHashString("by", this), new Integer(46));
	literals.put(new ANTLRHashString("as", this), new Integer(48));
}

public Token nextToken() throws TokenStreamException {
	Token theRetToken=null;
tryAgain:
	for (;;) {
		Token _token = null;
		int _ttype = Token.INVALID_TYPE;
		resetText();
		try {   // for char stream error handling
			try {   // for lexical error handling
				switch ( LA(1)) {
				case '\'':
				{
					mQUOTED_STRING(true);
					theRetToken=_returnToken;
					break;
				}
				case ';':
				{
					mSEMI(true);
					theRetToken=_returnToken;
					break;
				}
				case '.':
				{
					mDOT(true);
					theRetToken=_returnToken;
					break;
				}
				case ',':
				{
					mCOMMA(true);
					theRetToken=_returnToken;
					break;
				}
				case '*':
				{
					mASTERISK(true);
					theRetToken=_returnToken;
					break;
				}
				case '@':
				{
					mAT_SIGN(true);
					theRetToken=_returnToken;
					break;
				}
				case '(':
				{
					mOPEN_PAREN(true);
					theRetToken=_returnToken;
					break;
				}
				case ')':
				{
					mCLOSE_PAREN(true);
					theRetToken=_returnToken;
					break;
				}
				case '+':
				{
					mPLUS(true);
					theRetToken=_returnToken;
					break;
				}
				case '-':
				{
					mMINUS(true);
					theRetToken=_returnToken;
					break;
				}
				case '/':
				{
					mDIVIDE(true);
					theRetToken=_returnToken;
					break;
				}
				case '%':
				{
					mPERCENT(true);
					theRetToken=_returnToken;
					break;
				}
				case '|':
				{
					mVERTBAR(true);
					theRetToken=_returnToken;
					break;
				}
				case '?':
				{
					mQUESTION_MARK(true);
					theRetToken=_returnToken;
					break;
				}
				case ':':
				{
					mCOLONTYPE(true);
					theRetToken=_returnToken;
					break;
				}
				case '=':
				{
					mEQ(true);
					theRetToken=_returnToken;
					break;
				}
				case '!':  case '<':  case '^':
				{
					mNOT_EQ(true);
					theRetToken=_returnToken;
					break;
				}
				case '>':
				{
					mGT(true);
					theRetToken=_returnToken;
					break;
				}
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':
				{
					mFLOAT(true);
					theRetToken=_returnToken;
					break;
				}
				case '\t':  case '\n':  case '\r':  case ' ':
				{
					mWS(true);
					theRetToken=_returnToken;
					break;
				}
				default:
					if ((LA(1)=='i') && (LA(2)=='s') && (LA(3)==' ') && (LA(4)=='n') && (LA(5)=='u')) {
						mISNULL(true);
						theRetToken=_returnToken;
					}
					else if ((LA(1)=='i') && (LA(2)=='s') && (LA(3)==' ') && (LA(4)=='n') && (LA(5)=='o')) {
						mISNOTNULL(true);
						theRetToken=_returnToken;
					}
					else if ((_tokenSet_0.member(LA(1))) && (true) && (true)) {
						mIDENTIFIER(true);
						theRetToken=_returnToken;
					}
				else {
					if (LA(1)==EOF_CHAR) {uponEOF(); _returnToken = makeToken(Token.EOF_TYPE);}
				else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				}
				if ( _returnToken==null ) continue tryAgain; // found SKIP token
				_ttype = _returnToken.getType();
				_returnToken.setType(_ttype);
				return _returnToken;
			}
			catch (RecognitionException e) {
				throw new TokenStreamRecognitionException(e);
			}
		}
		catch (CharStreamException cse) {
			if ( cse instanceof CharStreamIOException ) {
				throw new TokenStreamIOException(((CharStreamIOException)cse).io);
			}
			else {
				throw new TokenStreamException(cse.getMessage());
			}
		}
	}
}

	public final void mISNULL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ISNULL;
		int _saveIndex;
		
		match("is null");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mISNOTNULL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ISNOTNULL;
		int _saveIndex;
		
		match("is not null");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mIDENTIFIER(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = IDENTIFIER;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case 'a':  case 'b':  case 'c':  case 'd':
		case 'e':  case 'f':  case 'g':  case 'h':
		case 'i':  case 'j':  case 'k':  case 'l':
		case 'm':  case 'n':  case 'o':  case 'p':
		case 'q':  case 'r':  case 's':  case 't':
		case 'u':  case 'v':  case 'w':  case 'x':
		case 'y':  case 'z':
		{
			matchRange('a','z');
			break;
		}
		case '"':
		{
			match('"');
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		{
		_loop181:
		do {
			switch ( LA(1)) {
			case '"':
			{
				match('"');
				break;
			}
			case 'a':  case 'b':  case 'c':  case 'd':
			case 'e':  case 'f':  case 'g':  case 'h':
			case 'i':  case 'j':  case 'k':  case 'l':
			case 'm':  case 'n':  case 'o':  case 'p':
			case 'q':  case 'r':  case 's':  case 't':
			case 'u':  case 'v':  case 'w':  case 'x':
			case 'y':  case 'z':
			{
				matchRange('a','z');
				break;
			}
			case '0':  case '1':  case '2':  case '3':
			case '4':  case '5':  case '6':  case '7':
			case '8':  case '9':
			{
				matchRange('0','9');
				break;
			}
			case '_':
			{
				match('_');
				break;
			}
			case '$':
			{
				match('$');
				break;
			}
			case '#':
			{
				match('#');
				break;
			}
			default:
			{
				break _loop181;
			}
			}
		} while (true);
		}
		_ttype = testLiteralsTable(_ttype);
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mQUOTED_STRING(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = QUOTED_STRING;
		int _saveIndex;
		
		match('\'');
		{
		_loop184:
		do {
			if ((_tokenSet_1.member(LA(1)))) {
				matchNot('\'');
			}
			else {
				break _loop184;
			}
			
		} while (true);
		}
		match('\'');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mSEMI(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = SEMI;
		int _saveIndex;
		
		match(';');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mDOT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DOT;
		int _saveIndex;
		
		match('.');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mCOMMA(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = COMMA;
		int _saveIndex;
		
		match(',');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mASTERISK(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = ASTERISK;
		int _saveIndex;
		
		match('*');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mAT_SIGN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = AT_SIGN;
		int _saveIndex;
		
		match('@');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mOPEN_PAREN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = OPEN_PAREN;
		int _saveIndex;
		
		match('(');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mCLOSE_PAREN(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = CLOSE_PAREN;
		int _saveIndex;
		
		match(')');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mPLUS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = PLUS;
		int _saveIndex;
		
		match('+');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mMINUS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = MINUS;
		int _saveIndex;
		
		boolean synPredMatched197 = false;
		if (((LA(1)=='-') && ((LA(2) >= '0' && LA(2) <= '9')) && (_tokenSet_2.member(LA(3))) && (_tokenSet_2.member(LA(4))) && (true))) {
			int _m197 = mark();
			synPredMatched197 = true;
			inputState.guessing++;
			try {
				{
				match('-');
				{
				int _cnt196=0;
				_loop196:
				do {
					if (((LA(1) >= '0' && LA(1) <= '9'))) {
						matchRange('0','9');
					}
					else {
						if ( _cnt196>=1 ) { break _loop196; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
					}
					
					_cnt196++;
				} while (true);
				}
				match('.');
				matchRange('0','9');
				}
			}
			catch (RecognitionException pe) {
				synPredMatched197 = false;
			}
			rewind(_m197);
inputState.guessing--;
		}
		if ( synPredMatched197 ) {
			match('-');
			{
			int _cnt199=0;
			_loop199:
			do {
				if (((LA(1) >= '0' && LA(1) <= '9'))) {
					matchRange('0','9');
				}
				else {
					if ( _cnt199>=1 ) { break _loop199; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				
				_cnt199++;
			} while (true);
			}
			match('.');
			{
			int _cnt201=0;
			_loop201:
			do {
				if (((LA(1) >= '0' && LA(1) <= '9'))) {
					matchRange('0','9');
				}
				else {
					if ( _cnt201>=1 ) { break _loop201; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				
				_cnt201++;
			} while (true);
			}
			{
			if ((LA(1)=='e')) {
				match('e');
				{
				switch ( LA(1)) {
				case '-':
				{
					match('-');
					break;
				}
				case '+':
				{
					match('+');
					break;
				}
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':
				{
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				{
				int _cnt205=0;
				_loop205:
				do {
					if (((LA(1) >= '0' && LA(1) <= '9'))) {
						matchRange('0','9');
					}
					else {
						if ( _cnt205>=1 ) { break _loop205; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
					}
					
					_cnt205++;
				} while (true);
				}
			}
			else {
			}
			
			}
			if ( inputState.guessing==0 ) {
				_ttype = FLOAT;
			}
		}
		else {
			boolean synPredMatched207 = false;
			if (((LA(1)=='-') && ((LA(2) >= '0' && LA(2) <= '9')) && (true) && (true) && (true))) {
				int _m207 = mark();
				synPredMatched207 = true;
				inputState.guessing++;
				try {
					{
					match('-');
					matchRange('0','9');
					}
				}
				catch (RecognitionException pe) {
					synPredMatched207 = false;
				}
				rewind(_m207);
inputState.guessing--;
			}
			if ( synPredMatched207 ) {
				match('-');
				{
				int _cnt209=0;
				_loop209:
				do {
					if (((LA(1) >= '0' && LA(1) <= '9'))) {
						matchRange('0','9');
					}
					else {
						if ( _cnt209>=1 ) { break _loop209; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
					}
					
					_cnt209++;
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					_ttype = INTEGER;
				}
			}
			else if ((LA(1)=='-') && (true)) {
				match('-');
			}
			else {
				throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
			}
			}
			if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
				_token = makeToken(_ttype);
				_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
			}
			_returnToken = _token;
		}
		
	public final void mDIVIDE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = DIVIDE;
		int _saveIndex;
		
		match('/');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mPERCENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = PERCENT;
		int _saveIndex;
		
		match('%');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mVERTBAR(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = VERTBAR;
		int _saveIndex;
		
		match('|');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mQUESTION_MARK(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = QUESTION_MARK;
		int _saveIndex;
		
		match('?');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mCOLONTYPE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = COLONTYPE;
		int _saveIndex;
		
		match("::");
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mEQ(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = EQ;
		int _saveIndex;
		
		match('=');
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mNOT_EQ(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = NOT_EQ;
		int _saveIndex;
		
		switch ( LA(1)) {
		case '<':
		{
			match('<');
			if ( inputState.guessing==0 ) {
				_ttype = LT;
			}
			{
			switch ( LA(1)) {
			case '>':
			{
				{
				match('>');
				if ( inputState.guessing==0 ) {
					_ttype = NOT_EQ;
				}
				}
				break;
			}
			case '=':
			{
				{
				match('=');
				if ( inputState.guessing==0 ) {
					_ttype = LE;
				}
				}
				break;
			}
			default:
				{
				}
			}
			}
			break;
		}
		case '!':
		{
			match("!=");
			break;
		}
		case '^':
		{
			match("^=");
			break;
		}
		default:
		{
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mGT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = GT;
		int _saveIndex;
		
		match('>');
		{
		if ((LA(1)=='=')) {
			match('=');
			if ( inputState.guessing==0 ) {
				_ttype = GE;
			}
		}
		else {
		}
		
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mFLOAT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = FLOAT;
		int _saveIndex;
		
		boolean synPredMatched226 = false;
		if ((((LA(1) >= '0' && LA(1) <= '9')) && (_tokenSet_2.member(LA(2))) && (_tokenSet_2.member(LA(3))) && (true) && (true))) {
			int _m226 = mark();
			synPredMatched226 = true;
			inputState.guessing++;
			try {
				{
				{
				int _cnt225=0;
				_loop225:
				do {
					if (((LA(1) >= '0' && LA(1) <= '9'))) {
						matchRange('0','9');
					}
					else {
						if ( _cnt225>=1 ) { break _loop225; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
					}
					
					_cnt225++;
				} while (true);
				}
				match('.');
				matchRange('0','9');
				}
			}
			catch (RecognitionException pe) {
				synPredMatched226 = false;
			}
			rewind(_m226);
inputState.guessing--;
		}
		if ( synPredMatched226 ) {
			{
			int _cnt228=0;
			_loop228:
			do {
				if (((LA(1) >= '0' && LA(1) <= '9'))) {
					matchRange('0','9');
				}
				else {
					if ( _cnt228>=1 ) { break _loop228; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				
				_cnt228++;
			} while (true);
			}
			match('.');
			{
			int _cnt230=0;
			_loop230:
			do {
				if (((LA(1) >= '0' && LA(1) <= '9'))) {
					matchRange('0','9');
				}
				else {
					if ( _cnt230>=1 ) { break _loop230; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				
				_cnt230++;
			} while (true);
			}
			{
			if ((LA(1)=='e')) {
				match('e');
				{
				switch ( LA(1)) {
				case '-':
				{
					match('-');
					break;
				}
				case '+':
				{
					match('+');
					break;
				}
				case '0':  case '1':  case '2':  case '3':
				case '4':  case '5':  case '6':  case '7':
				case '8':  case '9':
				{
					break;
				}
				default:
				{
					throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
				}
				}
				}
				{
				int _cnt234=0;
				_loop234:
				do {
					if (((LA(1) >= '0' && LA(1) <= '9'))) {
						matchRange('0','9');
					}
					else {
						if ( _cnt234>=1 ) { break _loop234; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
					}
					
					_cnt234++;
				} while (true);
				}
			}
			else {
			}
			
			}
		}
		else if (((LA(1) >= '0' && LA(1) <= '9')) && (true) && (true) && (true) && (true)) {
			{
			int _cnt236=0;
			_loop236:
			do {
				if (((LA(1) >= '0' && LA(1) <= '9'))) {
					matchRange('0','9');
				}
				else {
					if ( _cnt236>=1 ) { break _loop236; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
				}
				
				_cnt236++;
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				_ttype = INTEGER;
			}
		}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	public final void mWS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
		int _ttype; Token _token=null; int _begin=text.length();
		_ttype = WS;
		int _saveIndex;
		
		{
		switch ( LA(1)) {
		case ' ':
		{
			match(' ');
			break;
		}
		case '\t':
		{
			match('\t');
			break;
		}
		case '\n':
		{
			match('\n');
			if ( inputState.guessing==0 ) {
				newline();
			}
			break;
		}
		default:
			if ((LA(1)=='\r') && (LA(2)=='\n')) {
				match('\r');
				match('\n');
				if ( inputState.guessing==0 ) {
					newline();
				}
			}
			else if ((LA(1)=='\r') && (true)) {
				match('\r');
				if ( inputState.guessing==0 ) {
					newline();
				}
			}
		else {
			throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
		}
		}
		}
		if ( inputState.guessing==0 ) {
			_ttype = Token.SKIP;
		}
		if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
			_token = makeToken(_ttype);
			_token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
		}
		_returnToken = _token;
	}
	
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 17179869184L, 576460743713488896L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { -549755813896L, -1L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 288019269919178752L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	
	}
