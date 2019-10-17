%Copied from Octave signal package https://octave.sourceforge.io/signal/index.html
function y = filtfilt(b, a, x)

  if (nargin ~= 3)
    print_usage;
  end
  rotate = (size(x,1)==1);
  if rotate                    % a row vector
    x = x(:);                   % make it a column vector
  end

  lx = size(x,1);
  a = a(:).';
  b = b(:).';
  lb = length(b);
  la = length(a);
  n = max(lb, la);
  lrefl = 3 * (n - 1);
  if la < n, a(n) = 0; end
  if lb < n, b(n) = 0; end

  if (size(x,1) <= lrefl)
    error ('filtfilt: X must be a vector or matrix with length greater than %d', lrefl);
  end

  % Compute a the initial state taking inspiration from
  % Likhterov & Kopeika, 2003. "Hardware-efficient technique for
  %     minimizing startup transients in Direct Form II digital filters"
  kdc = sum(b) / sum(a);
  if (abs(kdc) < inf) % neither NaN nor +/- Inf
    si = fliplr(cumsum(fliplr(b - kdc * a)));
  else
    si = zeros(size(a)); % fall back to zero initialization
  end
  si(1) = [];
    
%   jsi = javaMethod('prepState','timo.jyu.Utils',b,a,zeros(length(a)-1,1));
%   [si', jsi]
  
%   keyboard;
  for (c = 1:size(x,2)) % filter all columns, one by one
%       keyboard;
    v = [2*x(1,c)-x((lrefl+1):-1:2,c); x(:,c);
         2*x(end,c)-x((end-1):-1:end-lrefl,c)]; % a column vector
%      jv =  javaMethod('getMirrored','timo.jyu.Utils',x(:,c),3*(length(a)-1));
%      
%      
%      keyboard;
     % Do forward and reverse filtering
%      zeroState =  zeros(length(a)-1,1);
%      jv0 =javaMethod('filter','timo.jyu.Utils',b,a,v,zeroState);
%      v0 = filter(b,a,v,zeroState);
%      v0_ = filter(b,a,v);% forward filter
%     jv = javaMethod('filter','timo.jyu.Utils',b,a,v,si*v(1));
     v = filter(b,a,v,si*v(1));                   % forward filter
%     figure
%     plot(v0_,'k');
%     hold on;
%     plot(jv0,'r');
%     keyboard;
    
    v = flipud(filter(b,a,flipud(v),si*v(end))); % reverse filter
    y(:,c) = v((lrefl+1):(lx+lrefl));
  end

  if (rotate)                   % x was a row vector
    y = rot90(y);               % rotate it back
  end