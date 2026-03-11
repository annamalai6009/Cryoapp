import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Snowflake, Shield, Activity, Bell, ArrowRight } from 'lucide-react';

const Index = () => {
  const navigate = useNavigate();

  const features = [
    {
      icon: Activity,
      title: 'Real-Time Monitoring',
      description: 'Track temperature, humidity, and door status in real-time with instant updates.',
    },
    {
      icon: Bell,
      title: 'Smart Alerts',
      description: 'Get notified immediately when temperatures exceed safe thresholds.',
    },
    {
      icon: Shield,
      title: 'Secure & Reliable',
      description: 'Enterprise-grade security with 99.9% uptime guarantee.',
    },
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary/5 via-background to-accent/10">
      {/* Header */}
      <header className="container py-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-10 h-10 bg-primary rounded-lg flex items-center justify-center">
              <Snowflake className="w-6 h-6 text-primary-foreground" />
            </div>
            <span className="font-bold text-xl">Freezer Monitoring</span>
          </div>
          <div className="flex items-center gap-3">
            <Button variant="ghost" onClick={() => navigate('/login')}>
              Sign In
            </Button>
            <Button onClick={() => navigate('/signup')}>
              Get Started
            </Button>
          </div>
        </div>
      </header>

      {/* Hero */}
      <section className="container py-20 text-center">
        <div className="max-w-3xl mx-auto">
          <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-primary/10 text-primary text-sm font-medium mb-6">
            <Snowflake className="w-4 h-4" />
            Cold Chain Monitoring Made Simple
          </div>
          <h1 className="text-4xl sm:text-5xl lg:text-6xl font-bold tracking-tight mb-6">
            Keep Your Freezers
            <span className="text-primary"> Under Control</span>
          </h1>
          <p className="text-xl text-muted-foreground mb-8 max-w-2xl mx-auto">
            Monitor temperature, track alerts, and ensure compliance with our intelligent freezer monitoring platform. Protect your inventory 24/7.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Button size="lg" onClick={() => navigate('/signup')} className="gap-2">
              Start Free Trial
              <ArrowRight className="w-4 h-4" />
            </Button>
            <Button size="lg" variant="outline" onClick={() => navigate('/login')}>
              Sign In to Dashboard
            </Button>
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="container pb-20">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 max-w-5xl mx-auto">
          {features.map((feature) => (
            <Card key={feature.title} className="stat-card border-0 shadow-lg">
              <CardContent className="p-6">
                <div className="w-12 h-12 rounded-xl bg-primary/10 flex items-center justify-center mb-4">
                  <feature.icon className="w-6 h-6 text-primary" />
                </div>
                <h3 className="text-lg font-semibold mb-2">{feature.title}</h3>
                <p className="text-muted-foreground">{feature.description}</p>
              </CardContent>
            </Card>
          ))}
        </div>
      </section>

      {/* Stats */}
      <section className="container pb-20">
        <div className="bg-primary rounded-2xl p-8 sm:p-12 text-primary-foreground max-w-5xl mx-auto">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-8 text-center">
            <div>
              <p className="text-4xl font-bold">10K+</p>
              <p className="text-primary-foreground/80 mt-1">Freezers Monitored</p>
            </div>
            <div>
              <p className="text-4xl font-bold">99.9%</p>
              <p className="text-primary-foreground/80 mt-1">Uptime</p>
            </div>
            <div>
              <p className="text-4xl font-bold">500+</p>
              <p className="text-primary-foreground/80 mt-1">Businesses</p>
            </div>
            <div>
              <p className="text-4xl font-bold">&lt;1s</p>
              <p className="text-primary-foreground/80 mt-1">Alert Time</p>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-border">
        <div className="container py-8">
          <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
            <div className="flex items-center gap-2">
              <Snowflake className="w-5 h-5 text-primary" />
              <span className="font-semibold">Freezer Monitoring</span>
            </div>
            <p className="text-sm text-muted-foreground">
              © {new Date().getFullYear()} Freezer Monitoring. All rights reserved.
            </p>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default Index;
