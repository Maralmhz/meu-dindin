const AI_KEY = 'AIzaSyAbzCY3ijWXswEOvxFHHupzOlEv0bqei1g';
const AI_URL = 'https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent';

async function sendMessage(){
  const input=document.getElementById('chat-input');
  const msg=input.value.trim();
  if(!msg)return;
  input.value='';
  if(!document.getElementById('chat-modal').classList.contains('flex'))toggleChat();
  appendMessage(msg,'user');
  const tid=showTyping();
  const prompt=`Voce e a IA financeira do app "meu din din". Detecte se e uma transacao ou conversa. CATEGORIAS: RECEITA, FIXAS, CARTAO, ALIMENTACAO, TRANSPORTE, SAUDE, LAZER, OUTROS Se for transacao nova: {"action":"ADD","desc":"...","amount":0,"category":"...","obs":"..."} Se for atualizacao: {"action":"UPDATE","targetDesc":"...","newAmount":0} Se for conversa: {"action":"CHAT","reply":"..."} Retorne APENAS JSON valido, sem markdown, sem texto extra. ${getFinancialContext()} Mensagem do usuario: ${msg}`;
  try{
    const res=await fetch(AI_URL+`?key=${AI_KEY}`,{
      method:'POST',
      headers:{'Content-Type':'application/json'},
      body:JSON.stringify({contents:[{parts:[{text:prompt}]}],generationConfig:{temperature:0.2,maxOutputTokens:300}})
    });
    const data=await res.json();
    removeTyping(tid);
    let raw=(data?.candidates?.[0]?.content?.parts?.[0]?.text||'').replace(/```json/g,'').replace(/```/g,'').trim();
    let parsed;try{parsed=JSON.parse(raw);}catch(e){parsed={action:'CHAT',reply:raw||'Erro: '+JSON.stringify(data).substring(0,150)};}
    if(parsed.action==='ADD')showConfirm(parsed);
    else if(parsed.action==='UPDATE')handleUpdate(parsed);
    else appendMessage(parsed.reply||'Nao entendi, tente novamente!','ai');
  }catch(e){removeTyping(tid);appendMessage('Erro de conexao: '+e.message,'ai');}
}
