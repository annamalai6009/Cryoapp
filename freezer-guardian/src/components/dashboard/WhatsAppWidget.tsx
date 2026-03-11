import React from 'react';
import { MessageCircle, ArrowRight, Smartphone } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';

export const WhatsAppWidget = () => {
  // ✅ 1. Updated Number (International Format: 1 is US code)
  const WHATSAPP_NUMBER = "15556045625"; 
  
  // ✅ 2. Updated Pre-filled Message
  const PREFILLED_MESSAGE = "Hello"; 

  const handleOpenWhatsApp = () => {
    // Opens WhatsApp Web or App with the message ready to send
    window.open(`https://wa.me/${WHATSAPP_NUMBER}?text=${PREFILLED_MESSAGE}`, '_blank');
  };

  return (
    <Card className="bg-gradient-to-br from-green-50 to-white border-green-100 shadow-sm overflow-hidden relative">
      <div className="absolute top-0 right-0 p-4 opacity-10">
        <MessageCircle size={100} className="text-green-600" />
      </div>
      
      <CardContent className="p-6 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        
        {/* Left Side: Text & Icon */}
        <div className="flex gap-4">
          <div className="p-3 bg-green-100 rounded-full text-green-600 shrink-0">
            <Smartphone size={24} />
          </div>
          <div className="space-y-1">
            <h3 className="font-bold text-lg text-gray-900">Cryo Monitor Bot</h3>
            <p className="text-sm text-gray-600 max-w-md">
              Need to check your freezer status quickly? 
              <br />
              <span className="inline-block mt-1 p-1 bg-white border border-gray-200 rounded text-xs font-mono text-gray-700">
                Click "Chat" and send "Hello" to start!
              </span>
            </p>
          </div>
        </div>

        {/* Right Side: Button */}
        <Button 
          onClick={handleOpenWhatsApp}
          className="bg-green-600 hover:bg-green-700 text-white shadow-md transition-all shrink-0 gap-2 w-full sm:w-auto"
        >
          <MessageCircle size={18} />
          Chat on WhatsApp
          <ArrowRight size={16} className="opacity-70" />
        </Button>

      </CardContent>
    </Card>
  );
};