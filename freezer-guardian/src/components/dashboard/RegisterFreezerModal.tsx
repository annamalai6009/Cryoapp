import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Plus, Loader2 } from 'lucide-react';
import { freezerService } from '@/services/freezerService';
import toast from 'react-hot-toast';

const formSchema = z.object({
  name: z.string().min(2, 'Name must be at least 2 characters'),
  poNumber: z.string().min(3, 'PO Number is required'),
});

interface RegisterFreezerModalProps {
  onSuccess: () => void;
}

export const RegisterFreezerModal = ({ onSuccess }: RegisterFreezerModalProps) => {
  const [open, setOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      name: '',
      poNumber: '',
    },
  });

// Inside onSubmit function:
  const onSubmit = async (values: z.infer<typeof formSchema>) => {
  setIsLoading(true);

  try {

    const request = {
      name: values.name,
      poNumber: values.poNumber,
    };

    await freezerService.registerFreezer(request);

    toast.success('Freezer registered successfully');
    setOpen(false);
    form.reset();
    onSuccess();

  } catch (error) {
    console.error("Registration failed", error);
  } finally {
    setIsLoading(false);
  }
};

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button className="gap-2">
          <Plus className="w-4 h-4" />
          Register Freezer
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>Register New Freezer</DialogTitle>
          <DialogDescription>
            Add a new freezer unit to your fleet.
          </DialogDescription>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            
            <FormField
              control={form.control}
              name="name"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Freezer Name</FormLabel>
                  <FormControl>
                    <Input placeholder="e.g. Lab Freezer 1" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            
            <FormField
              control={form.control}
              name="poNumber"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>PO Number / Serial ID</FormLabel>
                  <FormControl>
                    <Input placeholder="e.g. PO-2024-001" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className="flex justify-end gap-3 pt-4">
              <Button type="button" variant="outline" onClick={() => setOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" disabled={isLoading}>
                {isLoading ? (
                  <>
                    <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                    Registering...
                  </>
                ) : (
                  'Register'
                )}
              </Button>
            </div>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
};