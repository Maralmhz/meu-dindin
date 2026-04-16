const AI_KEY = 'AQ.Ab8RN6L9qC58QBZ_qN9wk_1h3yQZP8_5JZJtlwD8YSyj2r3hbg';
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
    const txt=await res.text();
    removeTyping(tid);
    let data;
    try{data=JSON.parse(txt);}catch(e){appendMessage('Status '+res.status+': '+txt.substring(0,300),'ai');return;}
    let raw=(data?.candidates?.[0]?.content?.parts?.[0]?.text||'').replace(/```json/g,'').replace(/```/g,'').trim();
    let parsed;
    try{parsed=JSON.parse(raw);}catch(e){parsed={action:'CHAT',reply:raw||'Erro API: '+JSON.stringify(data).substring(0,300)};}
    if(parsed.action==='ADD')showConfirm(parsed);
    else if(parsed.action==='UPDATE')handleUpdate(parsed);
    else appendMessage(parsed.reply,'ai');
  }catch(e){removeTyping(tid);appendMessage('Erro de conexao: '+e.message,'ai');}
}
