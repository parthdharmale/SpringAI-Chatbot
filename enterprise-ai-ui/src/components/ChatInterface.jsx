import React, { useState, useRef, useEffect } from 'react';
import './ChatInterface.css';

const ChatInterface = () => {
    const [messages, setMessages] = useState([
        { sender: 'bot', text: 'Hello! I am your secure enterprise assistant. How can I help you today?' }
    ]);
    const [inputValue, setInputValue] = useState('');
    const [currentUser, setCurrentUser] = useState('userA');
    const [isTyping, setIsTyping] = useState(false);
    const messagesEndRef = useRef(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    const handleUserSwitch = (e) => {
        setCurrentUser(e.target.value);
        setMessages([{ sender: 'bot', text: 'Hello! I am your secure enterprise assistant. How can I help you today?' }]);
    };

    const sendMessage = () => {
        if (!inputValue.trim() || isTyping) return;

        const userMessage = inputValue;
        setInputValue('');
        
        setMessages(prev => [
            ...prev, 
            { sender: 'user', text: userMessage },
            { sender: 'bot', text: '' } 
        ]);
        
        setIsTyping(true);

        // Connect to your Spring Boot backend!
        const url = `http://localhost:8080/chat?sessionId=${encodeURIComponent(currentUser)}&message=${encodeURIComponent(userMessage)}`;
        const eventSource = new EventSource(url);

        // 3. Listen for incoming JSON tokens and append them dynamically
        eventSource.onmessage = (event) => {
            // Parse the JSON payload sent by Spring Boot
            const payload = JSON.parse(event.data);
            const incomingToken = payload.token;

            setMessages(prev => {
                const newMessages = [...prev];
                const lastIndex = newMessages.length - 1;
                // Append the perfectly spaced token to the last bot message
                newMessages[lastIndex] = {
                    ...newMessages[lastIndex],
                    text: newMessages[lastIndex].text + incomingToken
                };
                return newMessages;
            });
        };

        eventSource.onerror = () => {
            eventSource.close();
            setIsTyping(false);
        };
    };

    const handleKeyPress = (e) => {
        if (e.key === 'Enter') {
            sendMessage();
        }
    };

    return (
        <div className="chat-wrapper">
            <div className="chat-container">
                <div className="auth-bar">
                    <span>🔒 Secure Enterprise Portal</span>
                    <div>
                        <label>Login as: </label>
                        <select value={currentUser} onChange={handleUserSwitch}>
                            <option value="userA">User A (US Region)</option>
                            <option value="userB">User B (IN Region)</option>
                        </select>
                    </div>
                </div>

                <div className="chat-header">AI Support Agent</div>
                
                <div className="chat-history">
                    {messages.map((msg, index) => (
                        <div key={index} className={`message ${msg.sender}-msg`}>
                            {msg.text}
                        </div>
                    ))}
                    <div ref={messagesEndRef} />
                </div>
                
                <div className="input-area">
                    <input 
                        type="text" 
                        value={inputValue}
                        onChange={(e) => setInputValue(e.target.value)}
                        onKeyDown={handleKeyPress}
                        placeholder="Type your message..." 
                        disabled={isTyping}
                    />
                    <button onClick={sendMessage} disabled={isTyping}>➤</button>
                </div>
            </div>
        </div>
    );
};

export default ChatInterface;